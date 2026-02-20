# Browser Testing Agent Memory

## Flutter Web App Interaction

Flutter web apps render into a `<canvas>` — standard DOM selectors like `get_by_text` and `locator("text=...")` do NOT work.

### How to interact with Flutter web in Playwright

1. **Enable Flutter semantics** (unlocks the accessibility tree):
   ```python
   await page.evaluate("""() => {
       const p = document.querySelector('flt-semantics-placeholder');
       if (p) { p.style.top = '0px'; p.style.left = '0px'; p.click(); }
   }""")
   await page.wait_for_timeout(1500)
   ```
   Note: The placeholder may be off-screen — force it into position before clicking.

2. **Query the Flutter semantics tree** for element positions:
   ```python
   sem = await page.evaluate("""() => {
       return Array.from(document.querySelectorAll('flt-semantics')).map(el => {
           const rect = el.getBoundingClientRect();
           return { role: el.getAttribute('role') || '',
                    label: el.getAttribute('aria-label') || '',
                    x: Math.round(rect.x), y: Math.round(rect.y),
                    w: Math.round(rect.width), h: Math.round(rect.height) };
       }).filter(e => e.w > 0 && e.h > 0);
   }""")
   ```
   - Nav items (`role=tab`) always have labels (Dashboard, Workouts, etc.)
   - Content items often have empty labels — use coordinate clicks instead

3. **Use coordinate-based mouse clicks** for content items:
   ```python
   await page.mouse.click(x, y)
   ```
   Calculate coords: `cx = el['x'] + el['w'] // 2`, `cy = el['y'] + el['h'] // 2`

4. **Detect navigation** by checking if semantics element count changes:
   ```python
   before = await page.evaluate("() => document.querySelectorAll('flt-semantics').length")
   await page.mouse.click(cx, cy)
   after = await page.evaluate("() => document.querySelectorAll('flt-semantics').length")
   navigated = before != after
   ```

### DevicePreview Coordinate Notes
When Flutter app uses `DevicePreview`, the phone mockup offsets content inside a viewport.
- At 390x844 viewport (iPhone 14): phone screen starts at ~x=38, y=57
- Bottom nav bar: y~644-708, tabs at x = 38, 101, 164, 226, 289 (left edge of each tab)
- Content area: x=38-352, y=57-644

### Common Pitfall
- `wait_for_load_state("networkidle")` + `wait_for_timeout(2000)` often not enough for Flutter
- Use `extra_ms=3000-4000` for initial load, `2000-2500` for navigation transitions
- Spinners (CircularProgressIndicator) may persist if demo data never resolves — capture anyway

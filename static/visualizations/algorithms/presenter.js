/* presenter.js: the presenter toolbar for these visualizations.
   Adapted from the author's Slides presenter toolbar (v2.7.0). Nothing is on screen until a key asks for it:
     T toolbar · L laser · O or S spotlight · D pen · M marker · X clear ink · Ctrl/⌘+Z undo
     ? or H this list · Esc drop the tool and hide the toolbar
   Differences from Slides, because these are scrolling pages rather than slides: ink is pinned
   to the page, so it scrolls with the content; one colour set suits both dark and light pages.
   Optional page keys for the ? list: <script src="presenter.js" data-keys="→ Space: next step; ← Shift+Space: previous step">
   The page works without this file.
   Public copy: the single-letter tools stay off (WCAG 2.1.4) unless the page is opened with ?present,
   which this browser remembers; ?present=off turns them off again. */
(() => {
  'use strict';
  if (window.presenterToolsLoaded) return;
  window.presenterToolsLoaded = true;
  const COLORS = ['#ff5c5c', '#ffb224', '#3fcf6e', '#3ea8ff', '#b18cff', '#ffffff', '#16181d'];
  const store = {
    get: (k, d) => { try { return localStorage.getItem('presenter.' + k) ?? d; } catch { return d; } },
    set: (k, v) => { try { localStorage.setItem('presenter.' + k, v); } catch { } }
  };
  // Space on a keyboard-focused button presses it instead of stepping the animation: the page's own
  // key handlers listen on document, and this capture listener on window runs before them. A mouse
  // click drops focus from the button, so click-then-Space still steps, and Shift+Space still steps back.
  addEventListener('keydown', e => {
    if (e.key === ' ' && !e.shiftKey && e.target.closest?.('button,[role="button"]')) e.stopPropagation();
  }, true);
  addEventListener('click', e => { if (e.detail > 0) e.target.closest?.('button')?.blur(); }, true);
  const announce = () => document.querySelectorAll('#status').forEach(el => {
    if (!el.hasAttribute('aria-live')) el.setAttribute('aria-live', 'polite');
  });
  if (document.readyState === 'loading') document.addEventListener('DOMContentLoaded', announce); else announce();
  const params = new URLSearchParams(location.search);
  let on = store.get('enabled', '0') === '1';
  if (params.has('present')) {   // ?present turns the tools on; ?present=off (or 0, false, no) turns them off
    on = !/^(off|0|false|no)$/i.test(params.get('present'));
    store.set('enabled', on ? '1' : '0');   // remembered where storage works; the URL still applies without it
  }
  if (!on) return;
  const pageKeys = (document.currentScript?.dataset.keys || '').split(';')
    .map(s => s.split(/:(.*)/s)).filter(r => r[1]).map(([k, d]) => [k.trim(), d.trim()]);

  const CSS = `
.pz-spot{position:fixed;inset:0;z-index:2147483000;pointer-events:none;display:none;
  background:radial-gradient(circle at var(--x,50%) var(--y,50%),transparent var(--r,140px),rgba(0,0,0,.6) calc(var(--r,140px) + 48px))}
.pz-spot.on{display:block}
.pz-ink{position:fixed;left:0;top:0;z-index:2147483001;pointer-events:none;touch-action:none}
.pz-ink.light{mix-blend-mode:multiply}
.pz-ink.live{pointer-events:auto;cursor:crosshair}
.pz-laser{position:fixed;left:0;top:0;width:var(--d,14px);height:var(--d,14px);border-radius:50%;background:var(--lc,#ff5c5c);
  box-shadow:0 0 0 3px rgba(255,255,255,.55),0 0 18px 4px var(--lc,#ff5c5c);z-index:2147483002;pointer-events:none;display:none;transform:translate(-50%,-50%)}
.pz-laser.on{display:block}
html.pz-tool-laser,html.pz-tool-laser :not(#pz-bar):not(#pz-bar *){cursor:none!important}
#pz-bar{position:fixed;left:50%;bottom:18px;transform:translateX(-50%);z-index:2147483003;display:flex;align-items:center;gap:2px;
  --ink:#e6e4df;--paper:#15171b;--line:rgba(0,0,0,.18);--hover:rgba(0,0,0,.08);
  background:var(--ink);color:var(--paper);padding:6px 10px;border-radius:999px;box-shadow:0 12px 30px -10px rgba(0,0,0,.5);
  opacity:0;visibility:hidden;pointer-events:none;transition:opacity .25s,visibility .25s;font:13px/1.2 ui-sans-serif,system-ui,-apple-system,"Segoe UI",Roboto,Arial,sans-serif;
  user-select:none;-webkit-user-select:none;white-space:nowrap;box-sizing:border-box;margin:0;letter-spacing:normal;text-align:left}
#pz-bar.light{--ink:#1b1d20;--paper:#fbfbf9;--line:rgba(255,255,255,.18);--hover:rgba(255,255,255,.12)}
#pz-bar.on{opacity:1;visibility:visible;pointer-events:auto}
#pz-bar button,#pz-bar kbd,#pz-bar input,#pz-help *{all:revert;box-sizing:border-box}
#pz-bar button{font:inherit;color:inherit;background:none;border:0;margin:0;cursor:pointer;padding:.35em .7em;border-radius:999px;opacity:.85;line-height:1.2}
#pz-bar button:hover{background:var(--hover);opacity:1}
#pz-bar button:focus-visible{outline:2px solid currentColor;outline-offset:1px}
#pz-bar button.act{background:var(--paper);color:var(--ink);opacity:1}
#pz-bar .pz-sep{width:1px;height:18px;background:var(--line);margin:0 6px;flex:none}
#pz-bar .pz-grip{cursor:grab;opacity:.5;padding:0 6px;letter-spacing:-2px;touch-action:none}
#pz-bar button.pz-sw{width:16px;height:16px;border-radius:50%;padding:0;margin:0 2px;border:2px solid transparent;opacity:1;
  box-shadow:inset 0 0 0 1px rgba(128,128,128,.55)}
#pz-bar button.pz-sw.act{border-color:var(--paper)}
#pz-bar input.pz-size{width:70px;accent-color:var(--paper);margin:0 6px;padding:0;cursor:pointer}
#pz-bar kbd{font:500 .7em ui-monospace,"SF Mono",Menlo,Consolas,monospace;border:1px solid var(--line);border-bottom-width:2px;border-radius:4px;
  padding:0 .35em;margin-left:.3em;color:inherit;opacity:.6;background:none}
@media (max-width:720px){#pz-bar{left:8px;right:8px;transform:none;flex-wrap:wrap;justify-content:center;border-radius:20px}}
#pz-help{position:fixed;inset:0;z-index:2147483004;display:none;align-items:center;justify-content:center;background:rgba(0,0,0,.55);
  font:15px/1.45 ui-sans-serif,system-ui,-apple-system,"Segoe UI",Roboto,Arial,sans-serif}
#pz-help.on{display:flex}
#pz-help .pz-card{display:block;background:#fbfbf9;color:#1b1d20;border-radius:14px;padding:22px 26px;max-width:min(560px,calc(100vw - 32px));
  max-height:calc(100vh - 32px);overflow:auto;box-shadow:0 24px 60px -20px rgba(0,0,0,.6);text-align:left}
#pz-help h2{font-size:17px;font-weight:650;margin:0 0 10px}
#pz-help th{font-size:12px;font-weight:600;letter-spacing:.06em;text-transform:uppercase;color:#6b7078;text-align:left;padding:14px 0 4px}
#pz-help tr:first-child th{padding-top:2px}
#pz-help table{border-collapse:collapse;width:100%}
#pz-help td{padding:3px 0;vertical-align:top;color:#1b1d20}
#pz-help td:first-child{padding-right:18px;white-space:nowrap}
#pz-help kbd{font:500 .8em ui-monospace,"SF Mono",Menlo,Consolas,monospace;border:1px solid #d5d6d2;border-bottom-width:2px;border-radius:4px;padding:0 .35em;color:#4a4e54;background:#fff}
@media print{.pz-spot,.pz-ink,.pz-laser,#pz-bar,#pz-help{display:none!important}}`;

  const esc = s => s.replace(/[&<>"]/g, c => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;' })[c]);
  const keyCells = k => k.split(' ').map(x => `<kbd>${esc(x)}</kbd>`).join(' ');
  const TOOL_KEYS = [['T', 'presenter toolbar'], ['L', 'laser pointer'], ['O S', 'spotlight'], ['D', 'pen'], ['M', 'marker'],
    ['Ctrl+Z ⌘+Z', 'undo the last stroke'], ['X', 'clear all drawings'], ['Esc', 'drop the tool, hide the toolbar'], ['? H', 'this list']];
  const rows = list => list.map(([k, d]) => `<tr><td>${keyCells(k)}</td><td>${esc(d)}</td></tr>`).join('');

  function init() {
    const style = document.createElement('style');
    style.textContent = CSS;
    document.head.appendChild(style);
    const make = (tag, cls, html) => { const n = document.createElement(tag); n.className = cls; if (html) n.innerHTML = html; document.body.appendChild(n); return n; };
    const spot = make('div', 'pz-spot'), ink = make('canvas', 'pz-ink'), laser = make('div', 'pz-laser');
    const bar = make('div', '', `
      <span class="pz-grip" title="Drag to move">⋮⋮</span>
      <button type="button" data-tool="laser">Laser<kbd>L</kbd></button>
      <button type="button" data-tool="spot">Spotlight<kbd>O</kbd></button>
      <button type="button" data-tool="pen">Pen<kbd>D</kbd></button>
      <button type="button" data-tool="mark">Marker<kbd>M</kbd></button>
      <span class="pz-sep"></span>
      <span class="pz-swatches"></span>
      <input type="range" class="pz-size" min="1" max="30" value="3" aria-label="Stroke size">
      <button type="button" data-act="undo" title="Undo (Ctrl+Z)">Undo</button>
      <button type="button" data-act="clear" title="Clear drawings (X)">Clear</button>
      <span class="pz-sep"></span>
      <button type="button" data-act="help" title="Keyboard shortcuts (?)">?</button>`);
    bar.id = 'pz-bar';
    bar.setAttribute('role', 'toolbar');
    bar.setAttribute('aria-label', 'Presenter tools');
    const help = make('div', '', `<div class="pz-card" role="dialog" aria-label="Keyboard shortcuts">
      <h2>Keyboard shortcuts</h2><table>${pageKeys.length ? `<tr><th colspan="2">This page</th></tr>${rows(pageKeys)}<tr><th colspan="2">Presenter tools</th></tr>` : ''}${rows(TOOL_KEYS)}</table></div>`);
    help.id = 'pz-help';

    // Toolbar and ink colours follow the page: a light pill and plain ink on dark pages (as in
    // the Slides dark theme), a dark pill and multiplied ink on light ones.
    const lum = el => {
      for (; el; el = el.parentElement) {
        const s = getComputedStyle(el), c = s.backgroundColor.match(/[\d.]+/g);
        const fill = c && (c[3] === undefined || +c[3] > .5) ? c : s.backgroundImage.match(/rgba?\(([^)]+)\)/)?.[1].match(/[\d.]+/g);
        if (fill) return (.2126 * fill[0] + .7152 * fill[1] + .0722 * fill[2]) / 255;
      }
      return 1;
    };
    const light = lum(document.body) >= .5;
    bar.classList.toggle('light', light);
    ink.classList.toggle('light', light);

    const ctx = ink.getContext('2d'), S = { tool: null, strokes: [] };
    const t = { color: store.get('color', COLORS[0]), size: +store.get('size', 3) || 3 };
    if (!COLORS.includes(t.color)) t.color = COLORS[0];
    const swatches = bar.querySelector('.pz-swatches'), size = bar.querySelector('.pz-size');
    swatches.innerHTML = COLORS.map(c => `<button type="button" class="pz-sw${c === t.color ? ' act' : ''}" data-c="${c}" style="background:${c}" aria-label="Colour ${c}"></button>`).join('');
    size.value = t.size;

    // Strokes are stored in page coordinates and drawn with the scroll offset, so the canvas
    // stays viewport-sized while the ink stays on the content it marks.
    const page = e => ({ x: e.clientX + scrollX, y: e.clientY + scrollY });
    const sizeInk = () => {
      const w = document.documentElement.clientWidth, h = document.documentElement.clientHeight, d = devicePixelRatio || 1;
      ink.width = Math.round(w * d); ink.height = Math.round(h * d); ink.style.width = w + 'px'; ink.style.height = h + 'px';
      redraw();
    };
    const strokePath = st => {
      ctx.beginPath(); ctx.lineCap = ctx.lineJoin = 'round'; ctx.strokeStyle = st.color; ctx.lineWidth = st.size;
      ctx.globalAlpha = st.tool === 'mark' ? .45 : 1;
      st.pts.forEach((p, i) => i ? ctx.lineTo(p.x, p.y) : ctx.moveTo(p.x, p.y));
      if (st.pts.length === 1) ctx.lineTo(st.pts[0].x + .01, st.pts[0].y);   // a click leaves a dot
      ctx.stroke(); ctx.globalAlpha = 1;
    };
    let drawing = null;
    function redraw() {
      const d = devicePixelRatio || 1;
      ctx.setTransform(1, 0, 0, 1, 0, 0); ctx.clearRect(0, 0, ink.width, ink.height);
      ctx.setTransform(d, 0, 0, d, -scrollX * d, -scrollY * d);
      S.strokes.forEach(strokePath);
      if (drawing) strokePath(drawing);
    }
    let raf = 0;
    const redrawSoon = () => { if (!raf) raf = requestAnimationFrame(() => { raf = 0; redraw(); }); };
    ink.addEventListener('pointerdown', e => {
      if (S.tool !== 'pen' && S.tool !== 'mark') return;
      e.preventDefault(); ink.setPointerCapture(e.pointerId);
      drawing = { tool: S.tool, color: t.color, size: S.tool === 'mark' ? t.size * 4 : t.size, pts: [page(e)] };
      redraw();
    });
    ink.addEventListener('pointermove', e => { if (!drawing) return; drawing.pts.push(page(e)); redraw(); });
    const endStroke = () => { if (!drawing) return; S.strokes.push(drawing); drawing = null; redraw(); };
    ink.addEventListener('pointerup', endStroke);
    ink.addEventListener('pointercancel', endStroke);
    addEventListener('scroll', redrawSoon, { passive: true });
    addEventListener('resize', () => { sizeInk(); keepBarInView(); });
    sizeInk();

    const undo = () => { S.strokes.pop(); redraw(); };
    const clear = () => { S.strokes = []; redraw(); };
    const setTool = tool => {
      S.tool = S.tool === tool ? null : tool;
      const root = document.documentElement;
      [...root.classList].filter(c => c.startsWith('pz-tool-')).forEach(c => root.classList.remove(c));
      if (S.tool) root.classList.add('pz-tool-' + S.tool);
      ink.classList.toggle('live', S.tool === 'pen' || S.tool === 'mark');
      laser.classList.toggle('on', S.tool === 'laser'); spot.classList.toggle('on', S.tool === 'spot');
      bar.querySelectorAll('[data-tool]').forEach(b => b.classList.toggle('act', b.dataset.tool === S.tool));
      if (S.tool) { showBar(); if (last) follow(last); }
    };
    let barT, sticky = false;
    const hideBar = () => { bar.classList.remove('on'); sticky = false; clearTimeout(barT); };
    const showBar = keep => {
      sticky = sticky || !!keep; bar.classList.add('on'); keepBarInView(); clearTimeout(barT);
      if (!sticky) barT = setTimeout(() => { if (!bar.matches(':hover')) bar.classList.remove('on'); }, 3000);
    };
    const toggleBar = () => bar.classList.contains('on') ? hideBar() : showBar(true);
    bar.addEventListener('mouseleave', () => { if (!sticky && bar.classList.contains('on')) showBar(); });
    const openHelp = on => help.classList.toggle('on', on ?? !help.classList.contains('on'));
    help.addEventListener('click', e => { if (e.target === help) openHelp(false); });

    let last = null;
    const follow = p => {
      if (S.tool === 'laser') { laser.style.left = p.x + 'px'; laser.style.top = p.y + 'px'; laser.style.setProperty('--lc', t.color); laser.style.setProperty('--d', (t.size * 3 + 8) + 'px'); }
      if (S.tool === 'spot') { spot.style.setProperty('--x', p.x + 'px'); spot.style.setProperty('--y', p.y + 'px'); spot.style.setProperty('--r', (80 + t.size * 12) + 'px'); }
    };
    addEventListener('pointermove', e => { last = { x: e.clientX, y: e.clientY }; follow(last); }, { passive: true });

    bar.addEventListener('click', e => {
      const b = e.target.closest('button'); if (!b) return;
      e.stopPropagation();
      if (e.detail > 0) b.blur();   // a clicked button must not keep Space or Enter for itself
      if (b.dataset.tool) setTool(b.dataset.tool);
      else if (b.dataset.c) {
        t.color = b.dataset.c; store.set('color', t.color);
        swatches.querySelectorAll('.pz-sw').forEach(s => s.classList.toggle('act', s === b));
        if (last) follow(last);
      }
      else ({ undo, clear, help: () => openHelp(true) })[b.dataset.act]?.();
      showBar();
    });
    size.addEventListener('input', () => { t.size = +size.value; store.set('size', t.size); if (last) follow(last); showBar(); });
    size.addEventListener('pointerup', () => size.blur());   // arrow keys go back to the page

    const grip = bar.querySelector('.pz-grip'); let drag = null;
    grip.addEventListener('pointerdown', e => {
      const r = bar.getBoundingClientRect();
      drag = { dx: e.clientX - r.left, dy: e.clientY - r.top }; grip.setPointerCapture(e.pointerId);
      Object.assign(bar.style, { transform: 'none', left: r.left + 'px', top: r.top + 'px', right: 'auto', bottom: 'auto', width: r.width + 'px' });
    });
    grip.addEventListener('pointermove', e => {
      if (!drag) return;
      const r = bar.getBoundingClientRect();
      bar.style.left = Math.max(0, Math.min(e.clientX - drag.dx, innerWidth - r.width)) + 'px';
      bar.style.top = Math.max(0, Math.min(e.clientY - drag.dy, innerHeight - r.height)) + 'px';
    });
    grip.addEventListener('pointerup', () => drag = null);
    function keepBarInView() {
      if (bar.style.transform !== 'none') return;
      const r = bar.getBoundingClientRect();
      bar.style.left = Math.max(0, Math.min(r.left, innerWidth - r.width)) + 'px';
      bar.style.top = Math.max(0, Math.min(r.top, innerHeight - r.height)) + 'px';
    }

    const KEYS = { t: toggleBar, l: () => setTool('laser'), o: () => setTool('spot'), s: () => setTool('spot'), d: () => setTool('pen'),
      m: () => setTool('mark'), x: clear, '?': () => openHelp(), h: () => openHelp(),
      escape: () => { setTool(null); hideBar(); openHelp(false); } };
    const typing = el => !!el.closest?.('textarea,select,[contenteditable]:not([contenteditable="false"])') ||
      (el.tagName === 'INPUT' && !/^(range|checkbox|radio|button|submit|reset|color|file|image)$/.test(el.type));
    document.addEventListener('keydown', e => {
      if (typing(e.target)) {
        if (e.key === 'Escape') { e.target.blur(); return; }
        // A number box cannot hold letters (except e), so the tool letters still reach the tools.
        const letter = /^[a-df-z?]$/i.test(e.key) && !e.ctrlKey && !e.metaKey && !e.altKey;
        if (!(e.target.type === 'number' && letter)) return;
      }
      if ((e.ctrlKey || e.metaKey) && !e.altKey && !e.shiftKey && e.key.toLowerCase() === 'z') { e.preventDefault(); undo(); return; }
      if (e.ctrlKey || e.metaKey || e.altKey) return;
      const k = e.key.toLowerCase(), fn = KEYS[k];
      if (!fn) return;
      if (help.classList.contains('on') && !['escape', '?', 'h'].includes(k)) openHelp(false);
      e.preventDefault();
      if (!e.repeat) fn();
    });

    window.presenterTools = { setTool, undo, clear, toggleBar, showBar, hideBar, openHelp, state: S };
  }
  if (document.readyState === 'loading') document.addEventListener('DOMContentLoaded', init); else init();
})();

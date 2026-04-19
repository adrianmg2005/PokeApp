/* ==========================================================
   POKÉAPP — Main Application Script v2
   ========================================================== */

const API = '';
const TYPE_COLORS = {
    fire:'#F08030', water:'#6890F0', grass:'#78C850', electric:'#F8D030',
    ice:'#98D8D8', fighting:'#C03028', poison:'#A040A0', ground:'#E0C068',
    flying:'#A890F0', psychic:'#F85888', bug:'#A8B820', rock:'#B8A038',
    ghost:'#705898', dragon:'#7038F8', dark:'#705848', steel:'#B8B8D0',
    fairy:'#EE99AC', normal:'#A8A878'
};
const TYPE_COLORS_DARK = {
    fire:'#E62829', water:'#2980EF', grass:'#3FA129', electric:'#FAC000',
    ice:'#3DCEF3', fighting:'#FF8000', poison:'#9B69D9', ground:'#915121',
    flying:'#81B9EF', psychic:'#EF4179', bug:'#91A119', rock:'#AFA981',
    ghost:'#704170', dragon:'#5060E1', dark:'#624D4E', steel:'#60A1B8',
    fairy:'#EF70EF', normal:'#9FA19F'
};
const TYPE_NAMES_ES = {
    fire:'Fuego', water:'Agua', grass:'Planta', electric:'Eléctrico',
    ice:'Hielo', fighting:'Lucha', poison:'Veneno', ground:'Tierra',
    flying:'Volador', psychic:'Psíquico', bug:'Bicho', rock:'Roca',
    ghost:'Fantasma', dragon:'Dragón', dark:'Siniestro', steel:'Acero',
    fairy:'Hada', normal:'Normal'
};
const GAME_COLORS = {
    'Rojo':'#FF1111','Azul':'#1111FF','Amarillo':'#FFD733','Oro':'#DAA520',
    'Plata':'#C0C0C0','Cristal':'#4FD8DE','Rubí':'#A00000','Zafiro':'#0000A6',
    'Esmeralda':'#00A000','Rojo Fuego':'#FF7327','Verde Hoja':'#00DD00',
    'Diamante':'#AAAAFF','Perla':'#FFAAAA','Platino':'#999999',
    'Oro HeartGold':'#B69E00','Plata SoulSilver':'#C0C0E0',
    'Negro':'#444444','Blanco':'#E0E0E0','Negro 2':'#333333','Blanco 2':'#D0D0D0',
    'X':'#025DA6','Y':'#EA1A3E','Rubí Omega':'#CF3025','Zafiro Alfa':'#26649C',
    'Sol':'#F1912B','Luna':'#5599CA','Ultra Sol':'#E95B2B','Ultra Luna':'#226DB5',
    'Let\'s Go Pikachu':'#F5DA26','Let\'s Go Eevee':'#C88E33',
    'Espada':'#00A1E9','Escudo':'#BF004F','Diamante Brillante':'#44AAE0','Perla Reluciente':'#E080A0',
    'Leyendas: Arceus':'#3B6AA0','Escarlata':'#F34024','Púrpura':'#7A34A3'
};
const GEN_RANGES = [
    {label:'Gen 1', min:1, max:151}, {label:'Gen 2', min:152, max:251},
    {label:'Gen 3', min:252, max:386}, {label:'Gen 4', min:387, max:493},
    {label:'Gen 5', min:494, max:649}, {label:'Gen 6', min:650, max:721},
    {label:'Gen 7', min:722, max:809}, {label:'Gen 8', min:810, max:905},
    {label:'Gen 9', min:906, max:1025}
];

let allPokemon = [];
let allMoves = [];
let allAbilities = [];
let allItems = [];
let championsTeams = [];
let typesData = {};

// Persistent state
let favorites = JSON.parse(localStorage.getItem('pokeapp_favorites') || '[]');
let searchHistory = JSON.parse(localStorage.getItem('pokeapp_history') || '[]');

// Filter state (multi-select)
let activeTypeFilters = new Set();
let activeGenFilters = new Set();
let listMode = 'all';

// Pagination
let pokemonPage = 0;
const PAGE_SIZE = 60;
let currentFilteredList = [];
let isLoadingMore = false;

// ═══════════════════════════════════════════
//  INITIALIZATION
// ═══════════════════════════════════════════

document.addEventListener('DOMContentLoaded', () => {
    checkStatus();
    setupTabs();
    setupMobileMenu();
});

function setupMobileMenu() {
    const btn = document.getElementById('hamburger-btn');
    const sidebar = document.getElementById('app-sidebar');
    const overlay = document.getElementById('mobile-overlay');
    if (!btn || !sidebar || !overlay) return;
    btn.addEventListener('click', () => {
        btn.classList.toggle('open');
        sidebar.classList.toggle('mobile-open');
        overlay.classList.toggle('visible');
    });
    overlay.addEventListener('click', () => {
        btn.classList.remove('open');
        sidebar.classList.remove('mobile-open');
        overlay.classList.remove('visible');
    });
    // Close menu when a nav button is clicked
    sidebar.querySelectorAll('.nav-btn').forEach(navBtn => {
        navBtn.addEventListener('click', () => {
            if (window.innerWidth <= 768) {
                btn.classList.remove('open');
                sidebar.classList.remove('mobile-open');
                overlay.classList.remove('visible');
            }
        });
    });
}

async function checkStatus() {
    try {
        const res = await fetch(API + '/api/status');
        const data = await res.json();
        document.getElementById('loading-message').textContent = data.message;
        document.getElementById('loading-bar').style.width = data.percent + '%';
        document.getElementById('loading-percent').textContent = data.percent + '%';
        if (data.loaded) {
            await loadInitialData();
            const overlay = document.getElementById('loading-overlay');
            overlay.classList.add('fade-out');
            setTimeout(() => overlay.style.display = 'none', 500);
            document.getElementById('app').style.display = 'flex';
        } else {
            setTimeout(checkStatus, 500);
        }
    } catch {
        document.getElementById('loading-message').textContent = 'Conectando con el servidor...';
        setTimeout(checkStatus, 2000);
    }
}

async function loadInitialData() {
    const [pokemonRes, typesRes] = await Promise.all([
        fetch(API + '/api/pokemon'),
        fetch(API + '/api/types')
    ]);
    allPokemon = await pokemonRes.json();
    typesData = await typesRes.json();
    currentFilteredList = allPokemon;
    pokemonPage = 0;
    renderPokemonGrid(false);
    setupSearch();
    setupFilters();
    setupFilterToggle();
    setupListModeToggles();
}

function setupFilterToggle() {
    const btn = document.getElementById('filter-toggle');
    const panel = document.getElementById('filter-panel');
    if (!btn || !panel) return;
    btn.addEventListener('click', () => {
        btn.classList.toggle('open');
        panel.classList.toggle('open');
    });
}

// ═══════════════════════════════════════════
//  TABS (vertical sidebar nav)
// ═══════════════════════════════════════════

function setupTabs() {
    document.querySelectorAll('.nav-btn').forEach(btn => {
        btn.addEventListener('click', () => {
            document.querySelectorAll('.nav-btn').forEach(b => b.classList.remove('active'));
            document.querySelectorAll('.tab-content').forEach(c => c.classList.remove('active'));
            btn.classList.add('active');
            document.getElementById('tab-' + btn.dataset.tab).classList.add('active');
            const tab = btn.dataset.tab;
            if (tab === 'movimientos' && allMoves.length === 0) loadMovesList();
            if (tab === 'habilidades' && allAbilities.length === 0) loadAbilitiesList();
            if (tab === 'objetos' && allItems.length === 0) loadItemsList();
            if (tab === 'champions' && championsTeams.length === 0) loadChampionsTeams();
        });
    });
}

// ═══════════════════════════════════════════
//  POKÉDEX — FILTERS
// ═══════════════════════════════════════════

function setupFilters() {
    const typeRow = document.getElementById('type-filters');
    typeRow.innerHTML = Object.keys(TYPE_COLORS).map(t =>
        `<button class="filter-chip type-chip" data-type="${t}" style="background:${TYPE_COLORS[t]}">${TYPE_NAMES_ES[t]}</button>`
    ).join('');
    typeRow.querySelectorAll('.type-chip').forEach(btn => {
        btn.addEventListener('click', () => {
            const t = btn.dataset.type;
            if (activeTypeFilters.has(t)) { activeTypeFilters.delete(t); btn.classList.remove('selected'); }
            else { activeTypeFilters.add(t); btn.classList.add('selected'); }
            updateFilterBadge();
            applyFilters();
        });
    });

    const genRow = document.getElementById('gen-filters');
    genRow.innerHTML = GEN_RANGES.map((g, i) =>
        `<button class="filter-chip gen-chip" data-gen="${i}">${g.label}</button>`
    ).join('');
    genRow.querySelectorAll('.gen-chip').forEach(btn => {
        btn.addEventListener('click', () => {
            const g = parseInt(btn.dataset.gen);
            if (activeGenFilters.has(g)) { activeGenFilters.delete(g); btn.classList.remove('selected'); }
            else { activeGenFilters.add(g); btn.classList.add('selected'); }
            updateFilterBadge();
            applyFilters();
        });
    });
}

function updateFilterBadge() {
    const btn = document.getElementById('filter-toggle');
    const count = activeTypeFilters.size + activeGenFilters.size;
    let badge = btn.querySelector('.filter-badge');
    if (count > 0) {
        if (!badge) { badge = document.createElement('span'); badge.className = 'filter-badge'; btn.appendChild(badge); }
        badge.textContent = count;
    } else if (badge) { badge.remove(); }
}

function applyFilters() {
    const searchQ = document.getElementById('pokemon-search').value.toLowerCase().trim();
    let list = allPokemon;

    if (listMode === 'favs') {
        list = list.filter(p => favorites.includes(p.name));
    } else if (listMode === 'history') {
        const histNames = searchHistory.map(h => h.name);
        list = list.filter(p => histNames.includes(p.name));
        list.sort((a, b) => histNames.indexOf(a.name) - histNames.indexOf(b.name));
    }
    if (activeTypeFilters.size > 0) {
        list = list.filter(p => p.types && [...activeTypeFilters].every(t => p.types.includes(t)));
    }
    if (activeGenFilters.size > 0) {
        list = list.filter(p => [...activeGenFilters].some(gi => {
            const range = GEN_RANGES[gi];
            return p.id >= range.min && p.id <= range.max;
        }));
    }
    if (searchQ.length >= 1) {
        list = list.filter(p =>
            p.spanishName.toLowerCase().includes(searchQ) ||
            p.name.includes(searchQ) ||
            String(p.id) === searchQ
        );
    }
    currentFilteredList = list;
    pokemonPage = 0;
    renderPokemonGrid(false);
}

function setupListModeToggles() {
    const btnAll = document.getElementById('btn-show-all');
    const btnFavs = document.getElementById('btn-show-favs');
    const btnHist = document.getElementById('btn-show-history');
    btnAll.classList.add('active');
    btnAll.addEventListener('click', () => { listMode = 'all'; updateToggles(); applyFilters(); });
    btnFavs.addEventListener('click', () => { listMode = 'favs'; updateToggles(); applyFilters(); });
    btnHist.addEventListener('click', () => { listMode = 'history'; updateToggles(); applyFilters(); });

    function updateToggles() {
        [btnAll, btnFavs, btnHist].forEach(b => b.classList.remove('active'));
        if (listMode === 'all') btnAll.classList.add('active');
        else if (listMode === 'favs') btnFavs.classList.add('active');
        else btnHist.classList.add('active');
    }
}

// ═══════════════════════════════════════════
//  POKÉDEX — GRID + INFINITE SCROLL
// ═══════════════════════════════════════════

function renderPokemonGrid(append) {
    const grid = document.getElementById('pokemon-list');
    const start = pokemonPage * PAGE_SIZE;
    const slice = currentFilteredList.slice(start, start + PAGE_SIZE);

    const countEl = document.getElementById('grid-count');
    if (countEl) countEl.textContent = `${currentFilteredList.length} Pokémon`;

    const html = slice.map(p => `
        <div class="pokemon-grid-item" onclick="loadPokemon('${esc(p.name)}')">
            <img src="https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/${p.id}.png"
                 alt="${esc(p.spanishName)}" loading="lazy">
            <span class="id-label">#${String(p.id).padStart(3,'0')}</span>
            <span class="name">${esc(p.spanishName)}</span>
            ${p.types ? `<div class="grid-types">${p.types.map(t => `<span class="grid-type-dot" style="background:${TYPE_COLORS[t]}" title="${TYPE_NAMES_ES[t]}"></span>`).join('')}</div>` : ''}
        </div>
    `).join('');

    if (append) grid.insertAdjacentHTML('beforeend', html);
    else grid.innerHTML = html;
    isLoadingMore = false;

    grid.removeEventListener('scroll', onGridScroll);
    if (start + PAGE_SIZE < currentFilteredList.length) {
        grid.addEventListener('scroll', onGridScroll);
    }
}

function onGridScroll() {
    const grid = document.getElementById('pokemon-list');
    if (isLoadingMore) return;
    if (grid.scrollTop + grid.clientHeight >= grid.scrollHeight - 120) {
        isLoadingMore = true;
        pokemonPage++;
        renderPokemonGrid(true);
    }
}

// ═══════════════════════════════════════════
//  POKÉDEX — SEARCH & SUGGESTIONS
// ═══════════════════════════════════════════

function setupSearch() {
    setupPokemonSearch();
    setupMoveSearch();
    setupAbilitySearch();
    setupItemSearch();
    setupCompareSearch();
}

function setupPokemonSearch() {
    const input = document.getElementById('pokemon-search');
    let debounce;

    input.addEventListener('input', () => {
        clearTimeout(debounce);
        debounce = setTimeout(() => applyFilters(), 150);
    });

    input.addEventListener('keydown', e => {
        if (e.key === 'Enter') {
            const q = input.value.trim();
            if (q && currentFilteredList.length > 0) loadPokemon(currentFilteredList[0].name);
        }
    });

    document.addEventListener('click', e => {
        if (!e.target.closest('.panel-sidebar') && !e.target.closest('.compare-input-wrap')) {
            document.querySelectorAll('.suggestions-list').forEach(s => s.classList.remove('visible'));
        }
    });
}

function renderSuggestions(container, items, onClick) {
    if (items.length === 0) { container.classList.remove('visible'); return; }
    container.innerHTML = items.map(p => `
        <div class="suggestion-item" data-name="${esc(p.name)}">
            <img src="https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/${p.id}.png" alt="">
            <div>
                <div class="name">${esc(p.spanishName)}</div>
                <div class="id">#${String(p.id).padStart(3,'0')}</div>
            </div>
            <div class="types">
                ${(p.types || []).map(t => `<span class="type-badge" style="background:${TYPE_COLORS[t] || '#777'};font-size:9px;padding:2px 6px">${(TYPE_NAMES_ES[t] || t).slice(0,3).toUpperCase()}</span>`).join('')}
            </div>
        </div>
    `).join('');
    container.classList.add('visible');
    container.querySelectorAll('.suggestion-item').forEach(el => {
        el.addEventListener('click', () => {
            onClick({ name: el.dataset.name });
            container.classList.remove('visible');
        });
    });
}

// ═══════════════════════════════════════════
//  POKÉDEX — DETAIL VIEW
// ═══════════════════════════════════════════

async function loadPokemon(name) {
    const detail = document.getElementById('pokemon-detail');
    detail.innerHTML = '<div class="empty-state"><div class="pokeball-spinner"></div></div>';
    addToHistory(name);

    try {
        const [pokeRes, speciesRes, evoRes, smogonRes] = await Promise.all([
            fetch(API + '/api/pokemon/' + encodeURIComponent(name)),
            fetch(API + '/api/pokemon/' + encodeURIComponent(name) + '/species'),
            fetch(API + '/api/pokemon/' + encodeURIComponent(name) + '/evolution'),
            fetch(API + '/api/pokemon/' + encodeURIComponent(name) + '/smogon')
        ]);
        const pokemon = await pokeRes.json();
        const species = await speciesRes.json();
        const evolution = await evoRes.json();
        const smogon = await smogonRes.json();

        if (pokemon.error) { detail.innerHTML = `<div class="empty-state"><p>${esc(pokemon.error)}</p></div>`; return; }
        renderPokemonDetail(pokemon, species, evolution, smogon);
    } catch {
        detail.innerHTML = '<div class="empty-state"><p>Error al cargar</p></div>';
    }
}

function addToHistory(name) {
    searchHistory = searchHistory.filter(h => h.name !== name);
    const p = allPokemon.find(pk => pk.name === name);
    searchHistory.unshift({ name, spanishName: p ? p.spanishName : name, time: Date.now() });
    if (searchHistory.length > 50) searchHistory.length = 50;
    localStorage.setItem('pokeapp_history', JSON.stringify(searchHistory));
}

function toggleFavorite(name) {
    const wasFav = favorites.includes(name);
    if (wasFav) favorites = favorites.filter(n => n !== name);
    else favorites.push(name);
    localStorage.setItem('pokeapp_favorites', JSON.stringify(favorites));
    const btn = document.getElementById('fav-btn');
    if (btn) { btn.textContent = !wasFav ? '★' : '☆'; btn.classList.toggle('is-fav', !wasFav); }
    const p = allPokemon.find(pk => pk.name === name);
    const displayName = p ? p.spanishName : name;
    if (!wasFav) showToast(`${displayName} añadido a favoritos`, '⭐');
    else showToast(`${displayName} eliminado de favoritos`, '💔');
}

function renderPokemonDetail(p, species, evolution, smogon) {
    const detail = document.getElementById('pokemon-detail');
    const stats = p.stats;
    const isFav = favorites.includes(p.name);

    let html = `
    <div class="card">
        <div class="pokemon-header">
            <div>
                <div class="pokemon-sprite-container">
                    <img id="pokemon-sprite" src="${esc(p.spriteArtwork)}" alt="${esc(p.spanishName)}">
                </div>
                <div class="sprite-controls">
                    <button class="active" onclick="switchSprite('${esc(p.spriteArtwork)}', this)">Artwork</button>
                    <button onclick="switchSprite('${esc(p.spriteArtworkShiny)}', this)">Shiny</button>
                    <button onclick="switchSprite('${esc(p.sprite)}', this)">Pixel</button>
                    <button onclick="switchSprite('${esc(p.spriteShiny)}', this)">Pixel ✨</button>
                </div>
            </div>
            <div class="pokemon-info-header">
                <h1>
                    <span class="pokemon-name-text">${esc(p.spanishName)}</span>
                    <span class="pokemon-id">#${String(p.id).padStart(3,'0')}</span>
                    <button id="fav-btn" class="fav-star ${isFav ? 'is-fav' : ''}" onclick="toggleFavorite('${esc(p.name)}')">${isFav ? '★' : '☆'}</button>
                </h1>
                <div class="pokemon-types">
                    ${p.types.map((t, i) => `<span class="type-badge type-badge-lg" style="background:${TYPE_COLORS[t]}">${esc(p.typesSpanish[i])}</span>`).join('')}
                </div>
                ${renderBadges(species)}
                ${species.genus ? `<p style="color:var(--text-secondary);margin-top:10px;font-style:italic;font-size:14px">${esc(species.genus)}</p>` : ''}
                ${species.description ? `<p style="color:var(--text-secondary);margin-top:8px;font-size:13px;max-width:420px;line-height:1.6">${esc(species.description)}</p>` : ''}
            </div>
        </div>

        <h3 class="section-title">Estadísticas Base</h3>
        <div class="stats-grid">
            ${renderStatRow('PS', stats.hp)}
            ${renderStatRow('Ataque', stats.attack)}
            ${renderStatRow('Defensa', stats.defense)}
            ${renderStatRow('At. Esp.', stats['special-attack'])}
            ${renderStatRow('Def. Esp.', stats['special-defense'])}
            ${renderStatRow('Velocidad', stats.speed)}
        </div>
        <div class="stat-total">Total: <span class="stat-total-value">${p.totalStats}</span></div>
    </div>`;

    // Abilities
    if (p.abilities && p.abilities.length > 0) {
        html += `<div class="card">
            <h3 class="section-title" style="color:var(--purple)">Habilidades</h3>
            <div style="display:flex;flex-wrap:wrap;gap:8px">
                ${p.abilities.map(a => `
                    <div class="ability-badge ${a.isHidden ? 'hidden-ability' : 'normal-ability'}"
                         onclick="switchToAbility('${esc(a.name)}')" title="${esc(a.description || '')}">
                        ${a.isHidden ? '🔒 ' : ''}${esc(a.spanishName)}
                    </div>
                `).join('')}
            </div>
        </div>`;
    }

    // Type Matchup
    if (p.typeMatchup) {
        html += `<div class="card"><h3 class="section-title">Eficacia de Tipo</h3>`;
        if (p.typeMatchup.weak && p.typeMatchup.weak.length > 0) {
            html += `<p class="matchup-label weak">Débil contra (×2 o más)</p>
                <div class="matchup-section">${p.typeMatchup.weak.map(t =>
                    `<span class="type-badge" style="background:${t.color}">${esc(t.spanishName)} ×${t.multiplier}</span>`
                ).join('')}</div>`;
        }
        if (p.typeMatchup.resist && p.typeMatchup.resist.length > 0) {
            html += `<p class="matchup-label resist">Resiste (×0.5 o menos)</p>
                <div class="matchup-section">${p.typeMatchup.resist.map(t =>
                    `<span class="type-badge" style="background:${t.color}">${esc(t.spanishName)} ×${t.multiplier}</span>`
                ).join('')}</div>`;
        }
        if (p.typeMatchup.immune && p.typeMatchup.immune.length > 0) {
            html += `<p class="matchup-label immune">Inmune</p>
                <div class="matchup-section">${p.typeMatchup.immune.map(t =>
                    `<span class="type-badge" style="background:${t.color}">${esc(t.spanishName)}</span>`
                ).join('')}</div>`;
        }
        html += `</div>`;
    }

    // Species Info
    if (species && !species.error) html += renderSpeciesInfo(species, p);

    // Held Items
    if (p.heldItems && p.heldItems.length > 0) {
        html += `<div class="card"><h3 class="section-title">Objetos Equipados</h3>
            <div style="display:flex;flex-wrap:wrap;gap:8px">
                ${p.heldItems.map(it => `<span class="item-chip" onclick="switchToItem('${esc(it)}')">${esc(formatItemName(it))}</span>`).join('')}
            </div></div>`;
    }

    // Game Versions
    if (p.games && p.games.length > 0) {
        html += `<div class="card"><h3 class="section-title">Aparece en</h3>
            <div class="games-grid">
                ${p.games.map(g => {
                    const c = GAME_COLORS[g] || '#888';
                    return `<span class="game-chip" style="background:${c}20;color:${c};border:1px solid ${c}40">${esc(g)}</span>`;
                }).join('')}
            </div></div>`;
    }

    // Evolution Chain
    if (evolution && evolution.paths && evolution.paths.length > 0) {
        html += `<div class="card"><h3 class="section-title">Cadena Evolutiva</h3>`;
        for (const path of evolution.paths) {
            html += `<div class="evo-chain">`;
            path.forEach((stage, i) => {
                if (i > 0) html += `<span class="evo-arrow">→</span>`;
                html += `<div class="evo-stage" onclick="loadPokemon('${esc(stage.name)}')">
                    <img src="https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/${stage.id}.png" alt="">
                    <span class="evo-name">${esc(stage.spanishName)}</span>
                    ${stage.method ? `<span class="evo-method">${esc(stage.method)}</span>` : ''}
                </div>`;
            });
            html += `</div>`;
        }
        html += `</div>`;
    }

    // Forms (Mega, Regional, Gigantamax, etc.)
    if (species.varieties && species.varieties.length > 1) {
        const forms = species.varieties.filter(v => v.name !== p.name);
        if (forms.length > 0) {
            html += `<div class="card"><h3 class="section-title">Formas y Variantes</h3>
                <div class="forms-grid">
                    ${forms.map(v => {
                        const label = getFormLabel(v.name, v.spanishName);
                        const spriteUrl = v.id ? `https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/${v.id}.png` : '';
                        return `<div class="form-card" onclick="loadPokemon('${esc(v.name)}')">
                            <img src="${spriteUrl}" alt="${esc(v.spanishName)}" onerror="this.style.opacity='0.3'">
                            <span class="form-name">${esc(v.spanishName)}</span>
                            <span class="form-label">${esc(label)}</span>
                        </div>`;
                    }).join('')}
                </div></div>`;
        }
    }

    // Smogon
    html += renderSmogonSets(smogon);
    detail.innerHTML = html;
}

function renderBadges(species) {
    if (!species) return '';
    let b = '';
    if (species.isLegendary) b += '<span class="badge badge-legendary">Legendario</span>';
    if (species.isMythical) b += '<span class="badge badge-mythical">Mítico</span>';
    if (species.isBaby) b += '<span class="badge badge-baby">Bebé</span>';
    return b ? `<div class="pokemon-badges">${b}</div>` : '';
}

const STAT_COLORS = {
    'PS':'#FF5555','Ataque':'#F5AC78','Defensa':'#FAE078',
    'At. Esp.':'#9DB7F5','Def. Esp.':'#A7DB8D','Velocidad':'#FA92B2'
};
function renderStatRow(name, value) {
    const pct = Math.min((value / 255) * 100, 100);
    const color = STAT_COLORS[name] || '#818cf8';
    return `<span class="stat-name">${name}</span><span class="stat-value" style="color:${color}">${value}</span>
        <div class="stat-bar-bg"><div class="stat-bar" style="width:${pct}%;background:${color}"></div></div>`;
}

function renderSpeciesInfo(species, p) {
    const items = [];
    if (species.habitat) items.push(['Hábitat', species.habitat]);
    if (species.color) items.push(['Color', species.color]);
    if (species.shape) items.push(['Forma', species.shape]);
    if (species.growthRate) items.push(['Crecimiento', species.growthRate]);
    if (species.captureRate >= 0) items.push(['Ratio Captura', species.captureRate]);
    if (species.baseHappiness >= 0) items.push(['Felicidad Base', species.baseHappiness]);
    if (species.hatchCounter >= 0) items.push(['Ciclos Eclosión', species.hatchCounter]);
    if (species.genderRate >= 0) {
        const f = (species.genderRate / 8 * 100).toFixed(0);
        items.push(['Género', species.genderRate === -1 ? 'Sin género' : `♂ ${100-f}% / ♀ ${f}%`]);
    }
    if (species.eggGroups && species.eggGroups.length > 0) items.push(['Grupo Huevo', species.eggGroups.join(', ')]);
    items.push(['Altura', (p.height / 10).toFixed(1) + ' m']);
    items.push(['Peso', (p.weight / 10).toFixed(1) + ' kg']);
    if (items.length === 0) return '';
    return `<div class="card"><h3 class="section-title">Información de Especie</h3>
        <div class="species-grid">${items.map(([l, v]) => `<div class="species-item"><div class="label">${l}</div><div class="value">${v}</div></div>`).join('')}</div></div>`;
}

function formatItemName(slug) {
    return slug.split('-').map(w => w.charAt(0).toUpperCase() + w.slice(1)).join(' ');
}

// ═══════════════════════════════════════════
//  SMOGON SETS
// ═══════════════════════════════════════════

function renderSmogonSets(smogon) {
    if (!smogon || smogon.source === 'none') return '';
    let html = `<div class="card"><h3 class="section-title section-title-smogon">Sets Competitivos</h3>`;
    if (smogon.source === 'smogon' && smogon.generations) {
        html += `<div class="gen-tabs">`;
        smogon.generations.forEach((gen, i) => { html += `<button class="gen-tab ${i===0?'active':''}" onclick="showSmogonGen(this,${i})">${esc(gen.genLabel)}</button>`; });
        html += `</div>`;
        smogon.generations.forEach((gen, i) => {
            html += `<div class="smogon-gen-content" ${i>0?'style="display:none"':''}>`;
            for (const set of gen.sets) html += renderSingleSet(set, true);
            html += `</div>`;
        });
    } else if (smogon.source === 'generated' && smogon.sets) {
        html += `<p style="color:var(--text-secondary);font-size:12px;margin-bottom:12px">Sets generados automáticamente</p>`;
        for (const set of smogon.sets) html += renderSingleSet(set, false);
    }
    html += `</div>`;
    return html;
}

function renderSingleSet(set, official) {
    let html = `<div class="smogon-card ${official?'official':''}">
        <h3 class="${official?'official':'generated'}">${esc(set.name)}</h3><div class="smogon-meta">`;
    if (set.ability) html += set.ability.map(a => `<span class="smogon-tag ability">${esc(a)}</span>`).join('');
    if (set.item) html += set.item.map(it => `<span class="smogon-tag item" onclick="switchToItem('${esc(it)}')">${esc(it)}</span>`).join('');
    if (set.nature) html += set.nature.map(n => `<span class="smogon-tag nature">${esc(n)}</span>`).join('');
    if (set.teratypes) html += set.teratypes.map(t => `<span class="smogon-tag teratype" style="background:${TYPE_COLORS[t.toLowerCase()]||'#777'}">${esc(t)}</span>`).join('');
    html += `</div>`;
    if (set.moves && set.moves.length > 0) {
        html += `<div class="smogon-moves">`;
        for (const slot of set.moves) {
            const label = slot.map(m => m.name || m).join(' / ');
            const type = slot[0].type || 'normal';
            html += `<span class="smogon-move" style="background:${TYPE_COLORS[type]||'#777'}" onclick="switchToMove('${esc(slot[0].slug||'')}')">${esc(label)}</span>`;
        }
        html += `</div>`;
    }
    if (set.evs) html += `<div class="smogon-evs">EVs: ${esc(set.evs)}</div>`;
    if (set.ivs) html += `<div class="smogon-evs">IVs: ${esc(set.ivs)}</div>`;
    html += `</div>`;
    return html;
}

function showSmogonGen(btn, idx) {
    const card = btn.closest('.card');
    card.querySelectorAll('.gen-tab').forEach(t => t.classList.remove('active'));
    btn.classList.add('active');
    card.querySelectorAll('.smogon-gen-content').forEach((c, i) => c.style.display = i === idx ? '' : 'none');
}

function switchSprite(url, btn) {
    document.getElementById('pokemon-sprite').src = url;
    btn.parentElement.querySelectorAll('button').forEach(b => b.classList.remove('active'));
    btn.classList.add('active');
}

// ═══════════════════════════════════════════
//  COMPARADOR
// ═══════════════════════════════════════════

function setupCompareSearch() {
    ['a', 'b'].forEach(side => {
        const input = document.getElementById('compare-' + side);
        const suggestions = document.getElementById('compare-suggestions-' + side);
        let debounce;
        input.addEventListener('input', () => {
            clearTimeout(debounce);
            debounce = setTimeout(() => {
                const q = input.value.toLowerCase().trim();
                if (q.length < 1) { suggestions.classList.remove('visible'); return; }
                const filtered = allPokemon.filter(p =>
                    p.spanishName.toLowerCase().includes(q) || p.name.includes(q) || String(p.id) === q
                ).slice(0, 8);
                renderSuggestions(suggestions, filtered, p => {
                    input.value = p.name;
                    suggestions.classList.remove('visible');
                });
            }, 150);
        });
        input.addEventListener('keydown', e => { if (e.key === 'Escape') suggestions.classList.remove('visible'); });
    });
    document.getElementById('compare-btn').addEventListener('click', comparePokemons);
}

async function comparePokemons() {
    const nameA = document.getElementById('compare-a').value.trim();
    const nameB = document.getElementById('compare-b').value.trim();
    if (!nameA || !nameB) return;
    const result = document.getElementById('compare-result');
    result.innerHTML = '<div class="empty-state"><div class="pokeball-spinner"></div></div>';
    try {
        const [resA, resB] = await Promise.all([
            fetch(API + '/api/pokemon/' + encodeURIComponent(nameA)).then(r => r.json()),
            fetch(API + '/api/pokemon/' + encodeURIComponent(nameB)).then(r => r.json())
        ]);
        if (resA.error || resB.error) { result.innerHTML = '<div class="empty-state"><p>Pokémon no encontrado</p></div>'; return; }
        renderComparison(resA, resB);
    } catch { result.innerHTML = '<div class="empty-state"><p>Error al comparar</p></div>'; }
}

function renderComparison(a, b) {
    const result = document.getElementById('compare-result');
    const sn = [['hp','PS'],['attack','Ataque'],['defense','Defensa'],['special-attack','At. Esp.'],['special-defense','Def. Esp.'],['speed','Velocidad']];
    const compStatColors = {'PS':'#FF5555','Ataque':'#F5AC78','Defensa':'#FAE078','At. Esp.':'#9DB7F5','Def. Esp.':'#A7DB8D','Velocidad':'#FA92B2'};

    let html = `<div class="compare-grid">`;
    [a, b].forEach(p => {
        const other = p === a ? b : a;
        html += `<div class="compare-panel card">
            <img src="${esc(p.spriteArtwork)}" alt="${esc(p.spanishName)}">
            <h2 style="color:var(--text);font-size:24px;font-weight:800">${esc(p.spanishName)}</h2>
            <div style="display:flex;gap:6px;justify-content:center;margin-bottom:14px">
                ${p.types.map((t, i) => `<span class="type-badge" style="background:${TYPE_COLORS[t]}">${esc(p.typesSpanish[i])}</span>`).join('')}
            </div>
            <div class="compare-stats">
                ${sn.map(([key, label]) => {
                    const v = p.stats[key], ov = other.stats[key];
                    const better = v > ov, worse = v < ov;
                    const baseColor = compStatColors[label] || '#818cf8';
                    const dimColor = baseColor + '88';
                    const color = better ? baseColor : worse ? dimColor : baseColor;
                    return `<div class="compare-stat-row">
                        <span class="label">${label}</span>
                        <span class="value" style="color:${baseColor}">${v} ${better?'▲':worse?'▼':'='}</span>
                        <div class="bar-bg"><div class="bar" style="width:${Math.min(v/255*100,100)}%;background:${better ? baseColor : dimColor};${better?'box-shadow:0 0 8px '+baseColor+'40':''};opacity:${worse?'0.5':'1'}"></div></div>
                    </div>`;
                }).join('')}
                <div class="compare-stat-row" style="margin-top:10px;border-top:1px solid var(--border);padding-top:10px">
                    <span class="label" style="font-size:13px;font-weight:800">Total</span>
                    <span class="value" style="font-size:16px;color:${p.totalStats >= other.totalStats?'var(--gold)':'var(--text-dim)'}">${p.totalStats}</span><div></div>
                </div>
            </div>
        </div>`;
    });
    html += `</div>`;

    // Verdict
    html += `<div class="compare-verdict card"><h3 class="section-title">Veredicto</h3><div class="species-grid">
        <div class="species-item"><div class="label">Más Stats Totales</div><div class="value" style="color:var(--gold)">${a.totalStats>b.totalStats?esc(a.spanishName):b.totalStats>a.totalStats?esc(b.spanishName):'Empate'} (${Math.max(a.totalStats,b.totalStats)})</div></div>
        <div class="species-item"><div class="label">Más Rápido</div><div class="value" style="color:var(--cyan)">${a.stats.speed>b.stats.speed?esc(a.spanishName):b.stats.speed>a.stats.speed?esc(b.spanishName):'Empate'}</div></div>
        <div class="species-item"><div class="label">Más Ataque Físico</div><div class="value">${a.stats.attack>b.stats.attack?esc(a.spanishName):b.stats.attack>a.stats.attack?esc(b.spanishName):'Empate'}</div></div>
        <div class="species-item"><div class="label">Más Ataque Especial</div><div class="value">${a.stats['special-attack']>b.stats['special-attack']?esc(a.spanishName):b.stats['special-attack']>a.stats['special-attack']?esc(b.spanishName):'Empate'}</div></div>
        <div class="species-item"><div class="label">Más Defensa Física</div><div class="value">${a.stats.defense>b.stats.defense?esc(a.spanishName):b.stats.defense>a.stats.defense?esc(b.spanishName):'Empate'}</div></div>
        <div class="species-item"><div class="label">Más Defensa Especial</div><div class="value">${a.stats['special-defense']>b.stats['special-defense']?esc(a.spanishName):b.stats['special-defense']>a.stats['special-defense']?esc(b.spanishName):'Empate'}</div></div>
        <div class="species-item"><div class="label">Más Bulk Físico</div><div class="value">${(a.stats.hp*a.stats.defense)>(b.stats.hp*b.stats.defense)?esc(a.spanishName):esc(b.spanishName)}</div></div>
        <div class="species-item"><div class="label">Más Bulk Especial</div><div class="value">${(a.stats.hp*a.stats['special-defense'])>(b.stats.hp*b.stats['special-defense'])?esc(a.spanishName):esc(b.spanishName)}</div></div>
    </div></div>`;

    // Defensive coverage
    if (a.typeMatchup && b.typeMatchup) {
        const aWeak = new Set((a.typeMatchup.weak||[]).map(t=>t.type));
        const bWeak = new Set((b.typeMatchup.weak||[]).map(t=>t.type));
        const aRes = new Set([...(a.typeMatchup.resist||[]).map(t=>t.type),...(a.typeMatchup.immune||[]).map(t=>t.type)]);
        const bRes = new Set([...(b.typeMatchup.resist||[]).map(t=>t.type),...(b.typeMatchup.immune||[]).map(t=>t.type)]);
        const aCoversB = [...bWeak].filter(t => aRes.has(t));
        const bCoversA = [...aWeak].filter(t => bRes.has(t));
        html += `<div class="compare-verdict card"><h3 class="section-title">Cobertura Defensiva</h3><div class="species-grid">
            <div class="species-item"><div class="label">${esc(a.spanishName)} cubre a ${esc(b.spanishName)}</div>
                <div class="value">${aCoversB.length>0?aCoversB.map(t=>`<span class="type-badge" style="background:${TYPE_COLORS[t]};font-size:10px;padding:2px 8px">${TYPE_NAMES_ES[t]}</span>`).join(' '):'Ninguna'}</div></div>
            <div class="species-item"><div class="label">${esc(b.spanishName)} cubre a ${esc(a.spanishName)}</div>
                <div class="value">${bCoversA.length>0?bCoversA.map(t=>`<span class="type-badge" style="background:${TYPE_COLORS[t]};font-size:10px;padding:2px 8px">${TYPE_NAMES_ES[t]}</span>`).join(' '):'Ninguna'}</div></div>
        </div></div>`;
    }

    result.innerHTML = html;
}

// ═══════════════════════════════════════════
//  MOVIMIENTOS
// ═══════════════════════════════════════════

async function loadMovesList() {
    const list = document.getElementById('move-list');
    list.innerHTML = '<div class="empty-state"><div class="pokeball-spinner"></div></div>';
    const res = await fetch(API + '/api/moves');
    allMoves = await res.json();
    renderMovesList(allMoves.slice(0, 150));
}

function renderMovesList(moves) {
    document.getElementById('move-list').innerHTML = moves.map(m => `
        <div class="item-list-entry" onclick="loadMoveDetail('${esc(m.slug)}')">
            <span class="type-badge" style="background:${TYPE_COLORS[m.type||'normal']||'#777'};font-size:9px;padding:2px 6px">${(TYPE_NAMES_ES[m.type]||m.type||'Normal').slice(0,3).toUpperCase()}</span>
            <span class="dmg-class-icon">${m.damageClass==='physical'?'💥':m.damageClass==='special'?'🔮':'📊'}</span>
            <span class="name">${esc(m.spanishName)}</span>
            <span class="meta">${m.power||'—'} / ${m.accuracy||'—'}</span>
        </div>
    `).join('');
}

function setupMoveSearch() {
    const input = document.getElementById('move-search');
    let debounce;
    input.addEventListener('input', () => {
        clearTimeout(debounce);
        debounce = setTimeout(() => {
            const q = input.value.toLowerCase().trim();
            if (q.length < 1) { renderMovesList(allMoves.slice(0, 150)); return; }
            renderMovesList(allMoves.filter(m => m.spanishName.toLowerCase().includes(q) || m.slug.includes(q)).slice(0, 80));
        }, 150);
    });
}

async function loadMoveDetail(slug) {
    const detail = document.getElementById('move-detail');
    detail.innerHTML = '<div class="empty-state"><div class="pokeball-spinner"></div></div>';
    const res = await fetch(API + '/api/moves/' + encodeURIComponent(slug));
    const m = await res.json();
    if (m.error) { detail.innerHTML = `<div class="empty-state"><p>${esc(m.error)}</p></div>`; return; }

    let html = `<div class="card">
        <div class="move-header">
            <h1 style="color:var(--text);font-size:28px;font-weight:800">${esc(m.spanishName)}</h1>
            <span class="type-badge type-badge-lg" style="background:${TYPE_COLORS[m.type]||'#777'}">${esc(m.typeSpanish)}</span>
            <span class="type-badge" style="background:rgba(0,0,0,0.06);color:var(--text-secondary)">${esc(m.damageClassSpanish)}</span>
            ${m.generation?`<span style="color:var(--text-dim);font-size:12px">${esc(m.generation)}</span>`:''}
        </div>
        ${m.description?`<p style="color:var(--text-secondary);margin-top:14px;line-height:1.6;font-size:14px">${esc(m.description)}</p>`:''}
        <div class="move-stats-grid">
            <div class="move-stat-card"><div class="label">Potencia</div><div class="value">${m.power||'—'}</div></div>
            <div class="move-stat-card"><div class="label">Precisión</div><div class="value">${m.accuracy||'—'}</div></div>
            <div class="move-stat-card"><div class="label">PP</div><div class="value">${m.pp||'—'}</div></div>
            <div class="move-stat-card"><div class="label">Prioridad</div><div class="value">${m.priority||'0'}</div></div>
        </div>
    </div>`;

    if (m.learnedBy && m.learnedBy.length > 0) {
        html += `<div class="card"><h3 class="section-title">Pokémon que lo aprenden (${m.learnedBy.length})</h3>
            <div class="pokemon-mini-grid">${m.learnedBy.map(p => `
                <div class="pokemon-mini-card" onclick="switchToPokedex('${esc(p.name)}')">
                    <img src="https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/${p.id}.png" alt="">
                    <span>${esc(p.spanishName)}</span>
                </div>`).join('')}
            </div></div>`;
    }
    detail.innerHTML = html;
}

// ═══════════════════════════════════════════
//  HABILIDADES
// ═══════════════════════════════════════════

async function loadAbilitiesList() {
    const list = document.getElementById('ability-list');
    list.innerHTML = '<div class="empty-state"><div class="pokeball-spinner"></div></div>';
    const res = await fetch(API + '/api/abilities');
    allAbilities = await res.json();
    renderAbilitiesList(allAbilities.slice(0, 100));
}

function renderAbilitiesList(abilities) {
    document.getElementById('ability-list').innerHTML = abilities.map(a => `
        <div class="item-list-entry" onclick="loadAbilityDetail('${esc(a.slug)}')">
            <span class="name" style="color:var(--purple)">${esc(a.spanishName)}</span>
        </div>
    `).join('');
}

function setupAbilitySearch() {
    const input = document.getElementById('ability-search');
    let debounce;
    input.addEventListener('input', () => {
        clearTimeout(debounce);
        debounce = setTimeout(() => {
            const q = input.value.toLowerCase().trim();
            if (q.length < 1) { renderAbilitiesList(allAbilities.slice(0, 100)); return; }
            renderAbilitiesList(allAbilities.filter(a => a.spanishName.toLowerCase().includes(q) || a.slug.includes(q)).slice(0, 50));
        }, 150);
    });
}

async function loadAbilityDetail(slug) {
    const detail = document.getElementById('ability-detail');
    detail.innerHTML = '<div class="empty-state"><div class="pokeball-spinner"></div></div>';
    const res = await fetch(API + '/api/abilities/' + encodeURIComponent(slug));
    const a = await res.json();
    if (a.error) { detail.innerHTML = `<div class="empty-state"><p>${esc(a.error)}</p></div>`; return; }

    let html = `<div class="card">
        <h1 style="color:var(--purple);font-size:26px;font-weight:800">${esc(a.spanishName)}</h1>
        <span style="color:var(--text-dim);font-size:12px">${esc(a.generation)}</span>
        ${a.description?`<p style="color:var(--text-secondary);margin-top:14px;line-height:1.6;font-size:14px">${esc(a.description)}</p>`:''}
        ${a.flavorText&&a.flavorText!==a.description?`<p style="color:var(--text-dim);margin-top:8px;font-style:italic;font-size:13px">${esc(a.flavorText)}</p>`:''}
    </div>`;

    if (a.pokemon && a.pokemon.length > 0) {
        html += `<div class="card"><h3 class="section-title">Pokémon con esta habilidad (${a.pokemon.length})</h3>
            <div class="pokemon-mini-grid">${a.pokemon.map(p => `
                <div class="pokemon-mini-card" onclick="switchToPokedex('${esc(p.name)}')">
                    <img src="https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/${p.id}.png" alt="">
                    <span>${esc(p.spanishName)}${p.isHidden?' 🔒':''}</span>
                </div>`).join('')}
            </div></div>`;
    }
    detail.innerHTML = html;
}

// ═══════════════════════════════════════════
//  OBJETOS
// ═══════════════════════════════════════════

async function loadItemsList() {
    document.getElementById('item-list').innerHTML = '<div class="empty-state"><div class="pokeball-spinner"></div></div>';
    const res = await fetch(API + '/api/items');
    allItems = await res.json();
    renderItemsList(allItems);
}

function renderItemsList(items) {
    document.getElementById('item-list').innerHTML = items.map(it => `
        <div class="item-list-entry" onclick="loadItemDetail('${esc(it.slug)}')">
            <span class="name" style="color:var(--cyan)">${esc(it.spanishName)}</span>
        </div>
    `).join('');
}

function setupItemSearch() {
    const input = document.getElementById('item-search');
    let debounce;
    input.addEventListener('input', () => {
        clearTimeout(debounce);
        debounce = setTimeout(() => {
            const q = input.value.toLowerCase().trim();
            if (q.length < 1) { renderItemsList(allItems); return; }
            renderItemsList(allItems.filter(it => it.spanishName.toLowerCase().includes(q) || it.slug.includes(q)));
        }, 150);
    });
}

async function loadItemDetail(slug) {
    const detail = document.getElementById('item-detail');
    detail.innerHTML = '<div class="empty-state"><div class="pokeball-spinner"></div></div>';
    const res = await fetch(API + '/api/items/' + encodeURIComponent(slug));
    const item = await res.json();
    if (item.error) { detail.innerHTML = `<div class="empty-state"><p>${esc(item.error)}</p></div>`; return; }

    let html = `<div class="card">
        <div class="item-header">
            ${item.spriteUrl?`<img src="${esc(item.spriteUrl)}" alt="">`:''}<div>
                <h1 style="color:var(--cyan);font-size:26px;font-weight:800">${esc(item.spanishName)}</h1>
                ${item.category?`<span style="color:var(--text-dim);font-size:12px">${esc(item.category)}</span>`:''}
            </div>
        </div>
        ${item.cost>0?`<p style="margin-top:14px;font-weight:700"><span style="color:var(--gold)">💰 ${item.cost} ₽</span></p>`:''}
        ${item.effect?`<p style="color:var(--text-secondary);margin-top:10px;line-height:1.6;font-size:14px">${esc(item.effect)}</p>`:''}
        ${item.flavorText?`<p style="color:var(--text-dim);margin-top:8px;font-style:italic;font-size:13px">${esc(item.flavorText)}</p>`:''}
    </div>`;

    if (item.heldBy && item.heldBy.length > 0) {
        html += `<div class="card"><h3 class="section-title">Pokémon que lo llevan</h3>
            <div class="pokemon-mini-grid">${item.heldBy.map(p => `
                <div class="pokemon-mini-card" onclick="switchToPokedex('${esc(p.name)}')">
                    <img src="https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/${p.id}.png" alt="">
                    <span>${esc(p.spanishName)}</span>
                </div>`).join('')}
            </div></div>`;
    }
    detail.innerHTML = html;
}

// ═══════════════════════════════════════════
//  CROSS-TAB NAVIGATION
// ═══════════════════════════════════════════

function switchToPokedex(name) {
    document.querySelectorAll('.nav-btn').forEach(b => b.classList.remove('active'));
    document.querySelectorAll('.tab-content').forEach(c => c.classList.remove('active'));
    document.querySelector('[data-tab="pokedex"]').classList.add('active');
    document.getElementById('tab-pokedex').classList.add('active');
    loadPokemon(name);
}

function switchToMove(slug) {
    if (!slug) return;
    document.querySelectorAll('.nav-btn').forEach(b => b.classList.remove('active'));
    document.querySelectorAll('.tab-content').forEach(c => c.classList.remove('active'));
    document.querySelector('[data-tab="movimientos"]').classList.add('active');
    document.getElementById('tab-movimientos').classList.add('active');
    if (allMoves.length === 0) loadMovesList().then(() => loadMoveDetail(slug));
    else loadMoveDetail(slug);
}

function switchToAbility(slug) {
    if (!slug) return;
    document.querySelectorAll('.nav-btn').forEach(b => b.classList.remove('active'));
    document.querySelectorAll('.tab-content').forEach(c => c.classList.remove('active'));
    document.querySelector('[data-tab="habilidades"]').classList.add('active');
    document.getElementById('tab-habilidades').classList.add('active');
    if (allAbilities.length === 0) loadAbilitiesList().then(() => loadAbilityDetail(slug));
    else loadAbilityDetail(slug);
}

function switchToItem(name) {
    if (!name) return;
    document.querySelectorAll('.nav-btn').forEach(b => b.classList.remove('active'));
    document.querySelectorAll('.tab-content').forEach(c => c.classList.remove('active'));
    document.querySelector('[data-tab="objetos"]').classList.add('active');
    document.getElementById('tab-objetos').classList.add('active');
    const slug = name.toLowerCase().replace(/ /g, '-');
    if (allItems.length === 0) loadItemsList().then(() => loadItemDetail(slug));
    else loadItemDetail(slug);
}

// ═══════════════════════════════════════════
//  UTILS
// ═══════════════════════════════════════════

function getFormLabel(name, spanishName) {
    if (name.includes('-mega-x')) return 'Mega X';
    if (name.includes('-mega-y')) return 'Mega Y';
    if (name.includes('-mega')) return 'Mega';
    if (name.includes('-gmax') || name.includes('-gigantamax')) return 'Gigantamax';
    if (name.includes('-alola')) return 'Alola';
    if (name.includes('-galar')) return 'Galar';
    if (name.includes('-hisui')) return 'Hisui';
    if (name.includes('-paldea')) return 'Paldea';
    if (name.includes('-primal')) return 'Primal';
    if (name.includes('-origin')) return 'Origen';
    if (name.includes('-therian')) return 'Tótem';
    if (name.includes('-black')) return 'Negro';
    if (name.includes('-white')) return 'Blanco';
    if (name.includes('-crowned')) return 'Coronado';
    if (name.includes('-eternamax')) return 'Eternamax';
    return 'Variante';
}

function esc(str) {
    if (str == null) return '';
    const div = document.createElement('div');
    div.textContent = String(str);
    return div.innerHTML;
}

// ═══════════════════════════════════════════
//  CHAMPIONS TEAMS
// ═══════════════════════════════════════════

const ARCHETYPE_COLORS = {
    'Rain':'#3b82f6', 'Sun':'#f59e0b', 'Sand':'#a16207', 'Snow':'#67e8f9',
    'Trick Room':'#a855f7', 'Hyper Offense':'#ef4444', 'Balance':'#22c55e'
};

let championsFilter = 'all';

async function loadChampionsTeams() {
    try {
        const res = await fetch(API + '/api/champions/teams');
        championsTeams = await res.json();
        renderChampionsFilters();
        renderChampionsGrid();
    } catch (e) {
        document.getElementById('champions-grid').innerHTML =
            '<div class="empty-state"><p>Error al cargar equipos</p></div>';
    }
}

function renderChampionsFilters() {
    const archetypes = [...new Set(championsTeams.map(t => t.archetype))].sort();
    const formats = [...new Set(championsTeams.map(t => t.format))].sort();
    const container = document.getElementById('champions-filters');
    container.innerHTML = `
        <div class="champ-filter-row">
            <button class="champ-filter-chip active" data-filter="all">Todos (${championsTeams.length})</button>
            ${formats.map(f => {
                const count = championsTeams.filter(t => t.format === f).length;
                return `<button class="champ-filter-chip format-chip" data-filter="format:${f}">${f} (${count})</button>`;
            }).join('')}
            ${archetypes.map(a => {
                const count = championsTeams.filter(t => t.archetype === a).length;
                const color = ARCHETYPE_COLORS[a] || '#6b7280';
                return `<button class="champ-filter-chip" data-filter="arch:${a}" style="--chip-color:${color}">${a} (${count})</button>`;
            }).join('')}
        </div>
    `;
    container.querySelectorAll('.champ-filter-chip').forEach(btn => {
        btn.addEventListener('click', () => {
            container.querySelectorAll('.champ-filter-chip').forEach(b => b.classList.remove('active'));
            btn.classList.add('active');
            championsFilter = btn.dataset.filter;
            renderChampionsGrid();
        });
    });
}

function getFilteredChampionsTeams() {
    if (championsFilter === 'all') return championsTeams;
    if (championsFilter.startsWith('format:')) {
        const fmt = championsFilter.slice(7);
        return championsTeams.filter(t => t.format === fmt);
    }
    if (championsFilter.startsWith('arch:')) {
        const arch = championsFilter.slice(5);
        return championsTeams.filter(t => t.archetype === arch);
    }
    return championsTeams;
}

function renderChampionsGrid() {
    const grid = document.getElementById('champions-grid');
    const teams = getFilteredChampionsTeams();
    if (teams.length === 0) {
        grid.innerHTML = '<div class="empty-state"><p>No hay equipos con este filtro</p></div>';
        return;
    }
    grid.innerHTML = teams.map((team, idx) => {
        const color = ARCHETYPE_COLORS[team.archetype] || '#6b7280';
        const pokemonIcons = team.pokemon.map(p => {
            const slug = p.name.toLowerCase()
                .replace('mega ', 'mega-').replace('alolan ', 'alola-')
                .replace('hisuian ', 'hisuian-').replace('heat ', 'heat-')
                .replace('wash ', 'wash-').replace('-therian', '-therian')
                .replace(/ /g, '-');
            return `<img class="champ-poke-icon" src="https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/${getPokemonIdFromName(slug)}.png" alt="${esc(p.name)}" title="${esc(p.name)}" onerror="this.src='https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/0.png'">`;
        }).join('');
        return `
        <div class="champ-team-card" data-team-idx="${idx}" onclick="showChampionsDetail(${idx})">
            <div class="champ-card-header">
                <div class="champ-card-badges">
                    <span class="champ-archetype-badge" style="background:${color}">${esc(team.archetype)}</span>
                    <span class="champ-format-badge">${esc(team.format)}</span>
                </div>
                <span class="champ-regulation">${esc(team.regulation)}</span>
            </div>
            <h3 class="champ-card-name">${esc(team.name)}</h3>
            ${team.author ? `<span class="champ-card-author">por ${esc(team.author)}</span>` : ''}
            <div class="champ-card-pokemon">${pokemonIcons}</div>
            <div class="champ-card-code-row">
                <span class="champ-card-code-label">Team ID</span>
                <code class="champ-card-code">${esc(team.teamCode)}</code>
                <button class="champ-copy-btn-small" onclick="event.stopPropagation(); copyTeamCode('${esc(team.teamCode)}')" title="Copiar código">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="14" height="14"><rect x="9" y="9" width="13" height="13" rx="2"/><path d="M5 15H4a2 2 0 01-2-2V4a2 2 0 012-2h9a2 2 0 012 2v1"/></svg>
                </button>
            </div>
        </div>`;
    }).join('');
}

function getPokemonIdFromName(slug) {
    // Try to find the Pokémon ID from allPokemon data
    const clean = slug.toLowerCase().replace(/ /g, '-');
    const found = allPokemon.find(p =>
        p.name === clean ||
        p.name === clean.replace('mega-', '') ||
        p.name.includes(clean.split('-')[0])
    );
    if (found) return found.id;
    // Fallback: guess from common Pokémon
    const knownIds = {
        'pelipper':279, 'archaludon':1018, 'basculegion':902, 'sneasler':903,
        'incineroar':727, 'rillaboom':812, 'mega-charizard-y':6, 'charizard':6,
        'venusaur':3, 'landorus-therian':645, 'porygon2':233, 'mega-meganium':154,
        'meganium':154, 'mega-floette':670, 'floette':670, 'sinistcha':1013,
        'mega-froslass':478, 'froslass':478, 'rotom-wash':479, 'rotom':479,
        'wash-rotom':479, 'heat-rotom':479, 'aerodactyl':142,
        'garchomp':445, 'kingambit':983, 'mega-tyranitar':248, 'tyranitar':248,
        'mega-garchomp':445, 'volcarona':637, 'mimikyu':778, 'corviknight':823,
        'mega-gengar':94, 'gengar':94, 'mega-lopunny':428, 'lopunny':428,
        'hippowdon':450, 'torkoal':324, 'hatterene':858, 'dusclops':356,
        'gyarados':130, 'mega-gyarados':130, 'glimmora':970, 'mega-starmie':121,
        'starmie':121, 'glaceon':471, 'primarina':730, 'ceruledge':937,
        'mega-venusaur':3, 'aegislash':681, 'hydreigon':635, 'raichu':26,
        'alolan-ninetales':38, 'alola-ninetales':38, 'ninetales':38,
        'greninja':658, 'mega-greninja':658, 'heatran':485, 'basculegion-male':902,
        'dragonite':149, 'mega-dragonite':149, 'azumarill':184, 'infernape':392,
        'espathra':956, 'mega-lucario':448, 'lucario':448, 'garganacl':934,
        'mega-scovillain':952, 'scovillain':952, 'skeledirge':911,
        'mega-chesnaught':652, 'chesnaught':652, 'meowscarada':908,
        'bellibolt':939, 'mega-clefable':36, 'clefable':36,
        'mega-victreebel':71, 'victreebel':71, 'mega-delphox':655, 'delphox':655,
        'hisuian-typhlosion':157, 'typhlosion':157, 'hisuian-arcanine':59, 'arcanine':59,
        'diggersby':660, 'ditto':132, 'mega-scizor':212, 'scizor':212,
        'mega-kangaskhan':115, 'kangaskhan':115,
        'maushold':925, 'talonflame':663, 'whimsicott':547,
        'mega-blastoise':9, 'blastoise':9,
        'mega-feraligatr':160, 'feraligatr':160,
        'mega-gardevoir':282, 'gardevoir':282,
        'excadrill':530
    };
    return knownIds[clean] || knownIds[clean.replace('mega-', '')] || 0;
}

function showChampionsDetail(idx) {
    const teams = getFilteredChampionsTeams();
    const team = teams[idx];
    if (!team) return;
    const overlay = document.getElementById('champions-detail-overlay');
    const panel = document.getElementById('champions-detail-panel');
    const color = ARCHETYPE_COLORS[team.archetype] || '#6b7280';

    const pokemonCards = team.pokemon.map(p => {
        const pokeid = getPokemonIdFromName(p.name.toLowerCase().replace(/ /g, '-'));
        const sprite = `https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/${pokeid}.png`;
        const evsList = [];
        if (p.evs) {
            if (p.evs.hp) evsList.push(`${p.evs.hp} HP`);
            if (p.evs.atk) evsList.push(`${p.evs.atk} Atk`);
            if (p.evs.def) evsList.push(`${p.evs.def} Def`);
            if (p.evs.spa) evsList.push(`${p.evs.spa} SpA`);
            if (p.evs.spd) evsList.push(`${p.evs.spd} SpD`);
            if (p.evs.spe) evsList.push(`${p.evs.spe} Spe`);
        }
        const teraColor = p.teraType ? (TYPE_COLORS[p.teraType.toLowerCase()] || '#888') : '#888';
        return `
        <div class="champ-pokemon-card">
            <div class="champ-pokemon-header">
                <img class="champ-pokemon-sprite" src="${sprite}" alt="${esc(p.name)}" onerror="this.src='https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/0.png'">
                <div class="champ-pokemon-info">
                    <h4 class="champ-pokemon-name">${esc(p.name)}</h4>
                    <div class="champ-pokemon-meta">
                        <span class="champ-meta-item" title="Objeto">🎒 ${esc(p.item)}</span>
                        <span class="champ-meta-item" title="Habilidad">⚡ ${esc(p.ability)}</span>
                        <span class="champ-meta-item" title="Naturaleza">🌿 ${esc(p.nature)}</span>
                        ${p.teraType ? `<span class="champ-meta-item champ-tera" style="color:${teraColor}" title="Tera Type">💎 ${esc(p.teraType)}</span>` : ''}
                    </div>
                </div>
            </div>
            <div class="champ-pokemon-moves">
                ${p.moves.map(m => `<span class="champ-move-tag">${esc(m)}</span>`).join('')}
            </div>
            ${evsList.length ? `<div class="champ-pokemon-evs"><span class="champ-evs-label">EVs:</span> ${evsList.join(' / ')}</div>` : ''}
        </div>`;
    }).join('');

    panel.innerHTML = `
        <button class="champ-detail-close" onclick="closeChampionsDetail()">&times;</button>
        <div class="champ-detail-header">
            <div>
                <div class="champ-detail-badges">
                    <span class="champ-archetype-badge" style="background:${color}">${esc(team.archetype)}</span>
                    <span class="champ-format-badge">${esc(team.format)}</span>
                    <span class="champ-regulation">${esc(team.regulation)}</span>
                </div>
                <h2 class="champ-detail-name">${esc(team.name)}</h2>
                ${team.author ? `<p class="champ-detail-author">por ${esc(team.author)}</p>` : ''}
            </div>
            <div class="champ-detail-code-box">
                <span class="champ-detail-code-label">Team ID — Copia y pega en Pokémon Champions</span>
                <div class="champ-detail-code-row">
                    <code class="champ-detail-code">${esc(team.teamCode)}</code>
                    <button class="btn-primary champ-copy-btn" onclick="copyTeamCode('${esc(team.teamCode)}')">
                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="16" height="16"><rect x="9" y="9" width="13" height="13" rx="2"/><path d="M5 15H4a2 2 0 01-2-2V4a2 2 0 012-2h9a2 2 0 012 2v1"/></svg>
                        Copiar
                    </button>
                </div>
            </div>
        </div>
        <div class="champ-detail-pokemon-grid">
            ${pokemonCards}
        </div>
    `;
    overlay.style.display = 'flex';
    requestAnimationFrame(() => overlay.classList.add('visible'));
}

function closeChampionsDetail() {
    const overlay = document.getElementById('champions-detail-overlay');
    overlay.classList.remove('visible');
    setTimeout(() => overlay.style.display = 'none', 250);
}

function copyTeamCode(code) {
    navigator.clipboard.writeText(code).then(() => {
        showToast(`Código ${code} copiado al portapapeles`, '📋');
    }).catch(() => {
        // Fallback
        const ta = document.createElement('textarea');
        ta.value = code;
        document.body.appendChild(ta);
        ta.select();
        document.execCommand('copy');
        document.body.removeChild(ta);
        showToast(`Código ${code} copiado al portapapeles`, '📋');
    });
}

// Close detail on overlay click
document.addEventListener('click', e => {
    if (e.target.id === 'champions-detail-overlay') closeChampionsDetail();
});
document.addEventListener('keydown', e => {
    if (e.key === 'Escape' && document.getElementById('champions-detail-overlay')?.classList.contains('visible')) {
        closeChampionsDetail();
    }
});

// ═══════════════════════════════════════════
//  TOAST NOTIFICATIONS
// ═══════════════════════════════════════════

function showToast(message, icon = '✨') {
    const container = document.getElementById('toast-container');
    if (!container) return;
    const toast = document.createElement('div');
    toast.className = 'toast';
    toast.innerHTML = `<span class="toast-icon">${icon}</span>${esc(message)}`;
    container.appendChild(toast);
    setTimeout(() => {
        toast.classList.add('toast-out');
        setTimeout(() => toast.remove(), 250);
    }, 3000);
}

// ═══════════════════════════════════════════
//  KEYBOARD SHORTCUTS
// ═══════════════════════════════════════════

document.addEventListener('keydown', e => {
    // Ctrl+K — focus Pokédex search
    if ((e.ctrlKey || e.metaKey) && e.key === 'k') {
        e.preventDefault();
        const search = document.getElementById('pokemon-search');
        if (search) {
            // Switch to Pokédex tab if not active
            const pokedexTab = document.getElementById('tab-pokedex');
            if (!pokedexTab.classList.contains('active')) {
                document.querySelectorAll('.nav-btn').forEach(b => b.classList.remove('active'));
                document.querySelectorAll('.tab-content').forEach(c => c.classList.remove('active'));
                document.querySelector('[data-tab="pokedex"]').classList.add('active');
                pokedexTab.classList.add('active');
            }
            search.focus();
            search.select();
        }
    }
    // Escape — clear search and close suggestions
    if (e.key === 'Escape') {
        document.querySelectorAll('.suggestions-list').forEach(s => s.classList.remove('visible'));
        if (document.activeElement && document.activeElement.classList.contains('search-input')) {
            document.activeElement.blur();
        }
    }
    // 1-5 — Quick tab switch (only when not focused on an input)
    if (!e.ctrlKey && !e.metaKey && !e.altKey && document.activeElement.tagName !== 'INPUT') {
        const tabKeys = {'1':'pokedex','2':'comparador','3':'movimientos','4':'habilidades','5':'objetos','6':'champions'};
        if (tabKeys[e.key]) {
            e.preventDefault();
            const btn = document.querySelector(`[data-tab="${tabKeys[e.key]}"]`);
            if (btn) btn.click();
        }
    }
});

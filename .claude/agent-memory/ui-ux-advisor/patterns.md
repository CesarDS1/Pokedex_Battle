# Design Patterns & Recurring Issues

## PokemonDetailScreen — Prioritized Issues (first review, 2026-02-22)

### HIGH
1. Hero area lacks type-color theming — header background is plain surface
2. Pokemon name is not capitalised (raw API lowercase string)
3. AsyncImage has no placeholder or error fallback
4. Stat bars use a single primary color regardless of value — no semantic feedback
5. Stat bars animate with no entrance animation (jarring on tab switch)

### MEDIUM
6. Name + #NNN on same Text line — no visual hierarchy between name and number
7. Gender display is a plain string — a visual ratio bar would be far more scannable
8. Cry button has an abrupt icon ↔ CircularProgressIndicator swap (no AnimatedContent)
9. About tab action buttons use generic icons (ArrowForward, List) — not meaningful
10. TopAppBar title duplicates the name already visible in the hero area

### LOW
11. GamesTab card text can overlap the Pokéball Canvas decoration
12. Scroll position resets each time user swaps tabs
13. MatchupSection label ("Weak to" etc.) has no spacing above it between sections
14. TypeBadge in About tab type row is not centered relative to the header area
15. No bottom padding guard for edge-to-edge on the last tab scroll content

## Component Patterns
- ElevatedCard is the consistent container style for all content cards
- WrappingRow used for multi-type badge layouts (MatchupsTab)
- Plain Row used for type badges in AboutTab (fine for ≤2 types, but WrappingRow is safer)

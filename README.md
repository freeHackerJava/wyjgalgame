# Sunday Coding Class

> Hefei, Autumn 2024. Four girls, one path to NOI Au.

## About the Game

*Sunday Coding Class* is a narrative management game set in the world of competitive programming (OI). You play as Wang Yijun, a third-year university student who takes on a Sunday tutoring job to earn some living expenses.

Four girls — Wu Yiming, Liu Zinuo, Zhu Haochen, and Li Jiadi — each have distinct personalities and learning rhythms. With limited action points each week, you must arrange teaching, mentoring, heart-to-heart talks, and mock contests to help them pass through CSP-S, NOIP, provincial team selection, and ultimately reach NOI.

The game isn't about min-maxing numbers — it's about understanding what each person fears, what they need, and how far a teacher can walk alongside their students.

## Key Features

- **Narrative Management**: 3 action points per week. Choose between "Teach Tech," "Mentor," "Heart-to-Heart," "Mock Contest," "Lesson Prep," "Group Review," and "Take a Walk." Stats decay over time, morale fluctuates — there is no one-size-fits-all solution.

- **Four Romanceable Characters**: Each has unique growth curves, exclusive relationship scenes, and endings. As favor increases (4 → 8 → 10 → 12), deeper conversations unlock.

- **New Game+ & Memory Collapse**: After the first clear, New Game+ unlocks with altered narrative versions of key scenes. From the third playthrough onward, management elements gradually fade into pure narrative as memories disintegrate.

- **Achievements & Global Progress**: Achievements are aggregated across all save slots. New Game+ unlocks permanently once triggered, and the main menu tracks overall progress.

## Story Structure

The game progresses through time and milestones:

- **Prologue**: First meeting with the four girls. Set the goal: "All four become NOI Au."

- **CSP-S / NOIP / Provincial Team / NOI**: Four critical checkpoints, each with passing thresholds and failure endings.

- **Relationship Scenes**: Each character unlocks exclusive scenes at favor levels 4, 8, 10, and 12, gradually deepening the bond.

- **New Game+ Variants**: Key scenes (Prologue, NOIP, Before Team Selection, Before NOI, NOI Ending) are replaced with "memory is fracturing" versions.

- **Third / Fourth Playthrough**: Pure narrative. Follow Wang Yijun's memory as it collapses — until someone finally sees him.

## Controls

### Main Menu

- **Save Slots**: 3 slots total. Start, continue, overwrite, or delete anytime.

- **New Game+**: Unlocked after completing a NOI failure ending in any slot. The "New Game+" button appears on the main menu.

- **Achievements**: Aggregated overview of all unlocked achievements across saves.

- **Settings**: Adjust text speed, auto-play delay, skip-read behavior, and fullscreen startup.

### Management Screen

- **Action Points**: 3 per week. Spend on teaching and activities.

- **Student Cards**: Display Ability, Tech, Favor, Morale, and Stability. Buttons correspond to four individual actions.

- **Group Actions**: Lesson Prep (all Tech +1), Group Review (all Ability +2), Take a Walk (all Morale + Favor), Mock Contest (all Ability + morale shifts).

- **End Week**: Advance time, trigger milestone checks, and random events.

- **Favor Scene Notification**: When a student's favor reaches a threshold, a prompt appears — click to enter the exclusive scene.

### Story Reader

- **Click Screen**: Reveal full text / advance to next line.

- **Bottom Toolbar**: Next, Skip Read, Fast-forward to next choice, Auto-play, History, Save Progress, Return to Main Menu.

- **Typewriter Animation**: Speed adjustable in Settings (0 = instant).

- **Choices**: Affect stats, favor, or story direction.

### Training Archive

Click "Training Archive" on the management screen to view current save data:
- Overview (week count, milestone progress, unlocked stories)
- Detailed student stats and favor stages
- Past milestone results and latest mock contest
- Key choices made
- Achievement wall

## Save Data

Saved in the user's home directory:

- **Windows**: `C:\Users\username\.wyjgalgame\`
- **macOS / Linux**: `~/.wyjgalgame/`

Files:
- `save-0.json`, `save-1.json`, `save-2.json`: Three save slots.
- `settings.json`: Global settings (text speed, auto-play, etc.).
- `progress.json`: Cross-save progress (New Game+ unlock, carried achievements).

## Tech Stack

- **Kotlin + Compose Multiplatform**: Desktop UI.
- **Voyager**: Navigation and screen management.
- **kotlinx.serialization**: JSON serialization for saves.
- **Pure Kotlin narrative engine**: Graph-based dialogue system supporting branches, choices, and state tracking.

## Content Scale

- **Main Story**: Prologue + 4 milestone scenes + 4 failure endings + 4 formal endings.
- **Relationship Scenes**: 4 per character (favor 4/8/10/12), 16 total.
- **New Game+ Variants**: 5 key scene replacements.
- **Third / Fourth Playthrough**: 2 long pure-narrative arcs.
- **Random Events**: ~20 types, triggered throughout gameplay.
- **Total dialogue nodes**: Over 600.

## About the "Multiple Playthroughs" Structure

The game spans four layers of playthroughs — not for grinding stats, but to show the same story across different "memory states":

- **First Playthrough**: Normal teaching experience. Management and narrative in balance.

- **New Game+**: Wang Yijun carries first-playthrough memories, but they begin to fracture. Management screen is simplified; key scenes replaced with "memory collapse" variants. Ends at NOI's collapse.

- **Third Playthrough**: Pure narrative. Management screen disappears. Wang Yijun's memory continues to disintegrate — competitive programming fades from 50% to 20% presence.

- **Fourth Playthrough**: Told from Wu Yiming's perspective. Walk back into that classroom using body memory and emotional fragments.

## FAQ

**Q: Why is the New Game+ management screen so simplified?**  
A: New Game+ shifts focus from "how to teach" to "memory is fracturing." Management still exists, but only two group actions remain — and stat decay deliberately outpaces gains, emphasizing the helplessness of "no matter how hard you try, you can't change the outcome."

**Q: How do I unlock the fourth playthrough?**  
A: After completing the third playthrough, a "I see you" entry appears in the corner of the New Game+ or fourth-playthrough ending panel.

**Q: What if I miss a relationship scene?**  
A: When favor reaches a threshold, the management screen displays an "Enter Relationship Scene" button. You can trigger it at any time. If skipped, it remains queued and can be activated later.

**Q: Are achievements shared across saves?**  
A: Yes. All unlocked achievement IDs are aggregated into global progress. The main menu's achievement overview shows the union across all slots.

## Credits

The four girls and that Hefei classroom are drawn from a distant but real memory. Thank you for taking the time to read slowly through code and words.

---

*"You are not here to copy her."*  
*— Wang Yijun*

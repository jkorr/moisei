# PROJECT MOISEI V0.5.0 (latest)
##### made by Daenil, 2014-?
##### updated on: 2015-04-02

## 1. ABOUT

Project Moisei (working title only) is fantasy dueling game of sorts in which you face various enemies which will try their best to end your life before you do the same with theirs. The game has a turn-based combat system and the player has to craft English words from a random set of 16 letters in order to defeat the endless waves of enemies.

## 2. CONTROLS

The game can be controlled either by keyboard or mouse [mouse input is disabled in this build!] and the mapping of the keys currently cannot be changed. The default mapping:

#### NORMAL CONTROLS
* ENTER : submit word and end turn / new wave (if all monsters are dead)
* BACKSPACE : remove the last letter from the letterbar
* 1-4 : use spells (fire, water, earth, wind)
* ARROW KEYS : (radial menu) select the letter in that direction
* SPACE : (radial menu) select the letter in the middle
* PGUP/PGDN : scroll the combatlog up and down
* ESC : leave the current game (press twice, no saving)
* F1 : toggle pause

#### DEBUG CONTROLS
* F2 : skip turn without submitting a word
* F3 : (cheat) add 1EP to all elements
* F11 : switch between debug screens
* F12 : toggle fps lock

## 3. HOW TO PLAY

You have a fixed amount of 16 letters in your inventory. Letters can either be assigned to one of the four elements (fire, water, earth, wind) or they can be neutral. Basic damage causes simply one hitpoint per letters. However, for using elemental letters you gain elemental power (EP), which can be used to cast spells. Using the right spells at the right time can decide the fate of the battle, as they provide extra damage, healing, incoming damage mitigation, or a way to manipulate the letters and the elements themselves. Whether you burn your EP with cheap spells to continuously enjoy their effects or save it up for the ultimates, it is up to you!

You can either click or type to pick the letters for your word, but at the current phase of development keyboard input has priority (which is a fancy way of saying "it should have a fewer amount of bugs"), so I recommend typing. ENTER submits your word, BACKSPACE removes the last letter from the letterbar, and if there are more copies of the same letter you can select the right one from the radial menu with the ARROW KEYS or SPACE.

## 4. KNOWN ISSUES

* [140914.1][UNSOLVED][min] Monster wait timer still uses the old system (ms, instead of ns)
* [150221.1][UNSOLVED][min] The last word of the enemy appears even though he didn't play anything because of a stun effect.
* [150311.1][UNSOLVED][min]	It is possible to select more than 10 letters making the game crash upon pressing ENTER.
* [150316.1][UNSOLVED][min]	It is possible to press ESC and leave by quitting (not by victory or defeat) while on the "endgame pause" screen.

## 5. CONTACT

Feel free to contact me at daenil@outlook.com if you have any issues, questions, or comments about the game.

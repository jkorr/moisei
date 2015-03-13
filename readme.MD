# PROJECT MOISEI V0.4.2 (latest) 
##### made by Daenil, 2014-?

## 1. ABOUT

Project Moisei (working title only) is fantasy dueling game of sorts in which you face various enemies which will try their best to end your life before you do the same with theirs. The game has a turn-based combat system and the player has to craft English words from a random set of 16 letters in order to defeat the endless waves of enemies.

## 2. CONTROLS

The game can be controlled either by keyboard or mouse [mouse input is disabled in this build!] and the mapping of the keys currently cannot be changed. The default mapping:

#### NORMAL CONTROLS
* ENTER - submit word and end turn / new wave (if all monsters are dead)
* BACKSPACE - remove the last letter from the letterbar
* F1 - toggle pause (currently it should not be used in the first turns, see 3. KNOWN ISSUES)
* ARROW KEYS - (radial menu) select the letter in that direction
* SPACE - (radial menu) select the letter in the middle

#### DEBUG CONTROLS
* F11 - toggle debug information
* F12 - toggle fps lock

#### DO-NOT-TOUCH
* F3 - drops the game, press F2 to reset it (may lead to issues, needs proper testing)

## 3. HOW TO PLAY

You have a fixed amount of 16 letters in your inventory. Letters can either be assigned to one of the four elements (fire, water, earth, wind) or they can be neutral. By combining letters of the same element, you can deal more damage, heal yourself up, stun your enemy for a turn or get damage reduction for the next attack the enemy throws at your face.

Basic damage is simply one hitpoint per letter, but elemental words modify that: for instance, fire words deal more damage, while water words are weaker than neutral ones to compensate for the healing. Wind words provide damage reduction for the next turn, but also deal less damage. Finally, earth words stun the enemy for one turn. In order to craft an elemental word, more than half of its letters should be of the same element. However, elemental words can have a minor effect when only half of the letters is of the same element (yes, this way you can make combos of two minor effects too).

You can either click or type to pick the letters for your word, but at the current phase of development keyboard input has priority (which is a fancy way of saying "it should have a fewer amount of bugs"), so I recommend typing. ENTER submits your word, BACKSPACE removes the last letter from the letterbar, and if there are more copies of the same letter you can select the right one from the radial menu with the ARROW KEYS or SPACE.

## 4. KNOWN ISSUES

* [140914.1][UNSOLVED][min] Monster wait timer still uses the old system (ms, instead of ns)
* [150201.1][UNSOLVED][maj] The new Q function (replace 2 random letters) is prone to crashes. I mean it crashes a lot.
* [150221.1][UNSOLVED][min] The last word of the enemy appears even though he didn't play anything because of a stun effect.

## 5. CONTACT

Feel free to contact me at daenil@outlook.com if you have any issues, questions, or comments about the game.
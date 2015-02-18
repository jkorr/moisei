##################################
# PROJECT MOISEI V0.4.1 (latest) #
##################################

made by Daenil, 2014-2015

--------
1. ABOUT
--------

Project Moisei (working title only) is a fantasy brawler of sorts in which you face various monsters which will try their best to end your life before you do the same with theirs. The game has a turn-based combat system and the player has to craft English words from a random set of letters in order to defeat the endless waves of enemies.

-----------
2. CONTROLS
-----------

Currently the games is controlled mostly by the mouse and the mapping of the keys cannot be changed. The default mapping:

// NORMAL CONTROLS
Q - replace 2 random letters at the cost of your turn
ENTER - end turn without action / new wave (if all monsters are dead)
P - toggle pause (currently it should not be used in the first turns, see 3. KNOWN ISSUES)
SPACE - [debug/old] hits the enemy with a basic attack, has nothing to do with letters and the new gameplay, do not use it!

// DEBUG CONTROLS
F1 - toggle old UI (old code, not useful at all)
F11 - toggle debug information
F12 - toggle fps lock
R - instant kill the enemy

PgUp - remove the last player ability
PgDn - add a random ability
M - spawn an additional monster (old code, should not be used at all)
N - drops the game, press M to reset it (may lead to issues, needs proper testing)

--------------
3. HOW TO PLAY
--------------

You have a fixed amount of 15 letters in your inventory. Letters can either be assigned to one of the four elements (fire, water, earth, wind) or they can be neutral. By combining letters of the same element, you can deal more damage, heal yourself up, stun your enemy for a turn or boost your next elemental word.
Basic damage is simply one hitpoint per letter, but elemental words modify that, for instance fire words deal more damage, but water words are weaker than neutral ones to compensate for the healing. Wind words are unique, they boost your next elemental word, making them more effective (do even more damage or healing). In order to craft an elemental word, more than half of its letters should be of the same element.

Also, for now, pressing Q replaces two random letters with new ones (they also get random elements!) at the cost of your turn. ENTER should not be used until the monster is alive (because pressing OK automatically ends your turn if the word you submitted is valid), since it will count as passing your turn.

---------------
4. KNOWN ISSUES
---------------

[140914.1][UNSOLVED][min] Monster wait timer still uses the old system (ms, instead of ns)

[150201.1][UNSOLVED][maj] The new Q function (replace 2 random letters) is prone to crashes. I mean it crashes a lot.

----------
5. CONTACT
----------

Feel free to contact me at daenil@outlook.com if you have any issues, questions, or comments about the game.
##################################
# PROJECT MOISEI V0.6.x (latest) #
##################################

made by Daenil, 2014

--------
1. ABOUT
--------

Project Moisei (working title only) is a fantasy brawler of sorts in which you face various monsters which will try their best to end your life before you do the same with theirs. The game has a turn-based combat system, so strategy will be more important than reflexes if you wish to survive the endless waves of enemies.

-----------
2. CONTROLS
-----------

Currently controls are restricted to keyboard only and the mapping cannot be changed. The default mapping:

// NORMAL CONTROLS
0 - cycle through targets
1-5 - select specific target (if there is an alive monster on that spot)
Q - player ability 1
W - player ability 2
E - player ability 3
R - player ability 4
SPACEBAR - player basic attack
ENTER - end turn / new wave (if all monsters are dead)
P - toggle pause (currently it should not be used in the first turns, see 3. KNOWN ISSUES)

// DEBUG CONTROLS
F1 - switch between normal and percentage-only view
F11 - toggle debug information
F12 - toggle fps lock

PgUp - remove the last player ability
PgDn - add a random ability
M - spawn an additional monster (should NOT be used when there are dead enemies on the screen!)

---------------
3. KNOWN ISSUES
---------------

[140810.1][UNSOLVED][min] Healing can be used even if you are at max health, there's no safety net there

[140811.2][UNSOLVED][min] If the player attacks with a weapon that does OT stuff, the OT stuff will not trigger if the player switches weapons (to be more precise, the OT stuff will only trigger while the player wields the weapon)

[140814.1][UNSOLVED][maj]DoTs/HoTs only affect the current target, so abilities like Firestorm are not working correctly

[140814.3][UNSOLVED][cri]Time management is still pretty unstable, for now players should not use manual pause ('P' key) in the first turn of any wave, as that definitely leads to issues (part of a temporary solution to other problems related to the timing code)

[140818.1][UNSOLVED][maj]If there are 2 monsters and you kill the "first" one, the other one won't attack anymore because the way endturn waiting is handled (the first one's wait will trigger, wait 1.5s and end the turn before the other's 3sec wait could end)

[140818.2][UNSOLVED][min]Mana value check is wrong, e.g. you can use a spell and have negative mana left (probably related to the introduction of spellpower)	

----------
4. CONTACT
----------

Feel free to contact me at daenil@outlook.com if you have any issues, questions, or comments about the game.
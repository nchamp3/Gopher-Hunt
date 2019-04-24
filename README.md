# Gopher-Hunt
Multithread guessing game to find the hidden gopher.

Description: 

    This game is essentially two threads playing against each other on a 10x10 game board. The threads take turns 
    guessing the hole with the gopher, and the first thread to find gopher wins.

    Game modes:
            1. 2 Player – User decides when each thread makes its guess.
            2. Continuous – Threads automatically make guesses.

**************************************************************************
Responses:

        1. Success — The thread correctly finds the gopher and wins the game.

        2. Near miss — The thread guesses one of the 8 holes adjacent to the gopher.
        
        3. Close guess — The thread guesses a hole 2 places away from the gopher.
        
        4. Disaster — The thread guesses a hole that has already been guessed.
        
        5. Complete miss — This response is shown in every other situation.

**************************************************************************

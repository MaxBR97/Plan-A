# Chess Game Model - Multiple Move Determination
# This model takes the current board state and determines a sequence of valid moves

# Define the board coordinates
set ROWS := {1 to 8};
set COLS := {1 to 8};
set BOARD := ROWS * COLS;

# Piece types
set PIECE_TYPES := {"PAWN", "KNIGHT", "BISHOP", "ROOK", "QUEEN", "KING"};
set COLORS := {"WHITE", "BLACK"};

# Parameter to determine which color to move (1 for white, 0 for black)
param computer_plays_as_white := 1;  # Change this to 0 to play as black

# Parameter to determine how many future moves to calculate
param future_depth := 4;  # Number of future moves to consider

# Set of move numbers for future moves
set FUTURE_MOVES := {0 to future_depth};

# Movement validation functions
# These only check if the move pattern is valid, ignoring other pieces

# Determine which color plays at each move number
# When computer_plays_as_white == 1:
#   White plays on odd moves (1,3,5...)
#   Black plays on even moves (2,4,6...)
# When computer_plays_as_white == 0:
#   Black plays on odd moves (1,3,5...)
#   White plays on even moves (2,4,6...)
defnumb is_playing_at_move(move,color) :=
    if computer_plays_as_white == 1 then
        if move mod 2 == 1 and color == "WHITE" then 1
        else if move mod 2 == 0 and color == "BLACK" then 1
        else 0 end end
    else
        if move mod 2 == 1 and color == "BLACK" then 1
        else if move mod 2 == 0 and color == "WHITE" then 1
        else 0 end end
    end;

# Check if coordinates are within board bounds
defnumb is_valid_position(r, c) :=
    if r >= 1 and r <= 8 and c >= 1 and c <= 8 then 1 else 0 end;

# Pawn move validation (without capture)
defnumb valid_pawn_move(r, c, dest_r, dest_c, is_white) :=
    if is_valid_position(dest_r, dest_c) == 0  then 0
    else            
        if is_white == "WHITE" then
            if dest_c != c then 0  # Must stay in same column
            else if r == 2 and dest_r == 4 and dest_c == c then 1  # First move two squares
            else if dest_r == r + 1 and dest_c == c then 1  # Regular move
            else 0 end end end
        else  # Black pawn
            if dest_c != c then 0  # Must stay in same column
            else if r == 7 and dest_r == 5 and dest_c == c then 1  # First move two squares
            else if dest_r == r - 1 and dest_c == c then 1  # Regular move
            else 0 end end end
        end
    end;

# Pawn capture validation
defnumb valid_pawn_eat_move(r, c, dest_r, dest_c, is_white) :=
    if is_valid_position(dest_r, dest_c) == 0 then 0
    else
        if is_white == "WHITE" then
            if dest_r == r + 1 and (dest_c == c + 1 or dest_c == c - 1) then 1
            else 0 end
        else
            if dest_r == r - 1 and (dest_c == c + 1 or dest_c == c - 1) then 1
            else 0 end
        end
    end;

# Rook move validation
defnumb valid_rook_move(r, c, dest_r, dest_c) :=
    if is_valid_position(dest_r, dest_c) == 0 then 0
    else
        if r == dest_r or c == dest_c then 1
        else 0 end
    end;

# Bishop move validation
defnumb valid_bishop_move(r, c, dest_r, dest_c) :=
    if is_valid_position(dest_r, dest_c) == 0 then 0
    else
        if abs(dest_r - r) == abs(dest_c - c) then 1
        else 0 end
    end;

# Knight move validation
defnumb valid_knight_move(r, c, dest_r, dest_c) :=
    if is_valid_position(dest_r, dest_c) == 0 then 0
    else
        if (abs(dest_r - r) == 2 and abs(dest_c - c) == 1) or
           (abs(dest_r - r) == 1 and abs(dest_c - c) == 2) then 1
        else 0 end
    end;

# Queen move validation (combination of rook and bishop)
defnumb valid_queen_move(r, c, dest_r, dest_c) :=
    if is_valid_position(dest_r, dest_c) == 0 then 0
    else
        if r == dest_r or c == dest_c or abs(dest_r - r) == abs(dest_c - c) then 1
        else 0 end
    end;

# King move validation
defnumb valid_king_move(r, c, dest_r, dest_c) :=
    if is_valid_position(dest_r, dest_c) == 0 then 0
    else
        if abs(dest_r - r) <= 1 and abs(dest_c - c) <= 1 then 1
        else 0 end
    end;


defstrg piece_display(type,color) := substr(color,0,1) + "_" + substr(type,0,10);
defstrg piece_display_reverse_piece(str) := substr(str,2,10); 
defstrg piece_display_reverse_color(str) := if substr(str,0,1) == "W" then "WHITE" else "BLACK" end; 

# Last move positions (current board state)
# Format: <row, col, piece_type, color>
set LAST_MOVE_POSITIONS_INPUT := {
    # White pieces (example starting positions)
    <2,1,"W_PAWN",1.0>,   # White pawn
    <1,1,"W_ROOK",1.0>,   # White rook
    <1,2,"W_KNIGHT",1.0>, # White knight
    <1,3,"W_BISHOP",1.0>, # White bishop
    <1,4,"W_QUEEN",1.0>,  # White queen
    <1,5,"W_KING",1.0>,   # White king
    # Add more pieces as needed...

    # Black pieces (example starting positions)
    <7,1,"B_PAWN",1.0>,   # Black pawn
    <8,1,"B_ROOK",1.0>,   # Black rook
    <8,2,"B_KNIGHT",1.0>, # Black knight
    <8,3,"B_BISHOP",1.0>, # Black bishop
    <8,4,"B_QUEEN",1.0>,  # Black queen
    <8,5,"B_KING",1.0>    # Black king
    # Add more pieces as needed...
};

set LAST_MOVE_POSITIONS := {<r,c,str> in proj(LAST_MOVE_POSITIONS_INPUT,<1,2,3>) | str != "O" : <r,c,piece_display_reverse_piece(str),piece_display_reverse_color(str)>};


set CURRENT_PIECES := proj(LAST_MOVE_POSITIONS,<3,4>);

set PIECES_DISPLAY := {<type,color> in CURRENT_PIECES: <piece_display(type,color)>} union {"O"};

# Variables for board states at different moves
# Move 0: Current position (last_move)
var last_move[BOARD * PIECES_DISPLAY] binary;

# Move 1: Next immediate move
var next_move[BOARD * PIECES_DISPLAY] binary;

# Moves 2 to future_depth: Future positions
var future_moves[FUTURE_MOVES * BOARD * CURRENT_PIECES] binary;

subto fill_empty_squares_last_move: forall <r,c> in BOARD:
    last_move[r,c,"O"] + (sum <t,color> in CURRENT_PIECES: last_move[r,c,piece_display(t,color)]) == 1;

subto fill_empty_squares_next_move: forall <r,c> in BOARD:
        next_move[r,c,"O"] + (sum <t,color> in CURRENT_PIECES: next_move[r,c,piece_display(t,color)]) == 1;

# Initialize last_move to match LAST_MOVE_POSITIONS
subto init_last_move: forall <r,c,t,color> in LAST_MOVE_POSITIONS:
    forall <r2,c2> in BOARD:
        last_move[r2,c2,piece_display(t,color)] == if r == r2 and c == c2 then 1 else 0 end;

subto init_last_move2: forall <r,c,t,color> in LAST_MOVE_POSITIONS:
    forall <r2,c2> in BOARD:
        last_move[r2,c2,piece_display(t,color)] == future_moves[0,r2,c2,t,color];

subto init_next_move: forall <r,c,t,color> in LAST_MOVE_POSITIONS:
    forall <r2,c2> in BOARD:
        next_move[r2,c2,piece_display(t,color)] == future_moves[1,r2,c2,t,color];

# Each piece must have either one position or be captured (sum <= 1)
subto one_position_future: forall <move> in FUTURE_MOVES:
    forall <t,color> in CURRENT_PIECES:
        (sum <r2,c2> in BOARD: future_moves[move,r2,c2,t,color]) <= 1;

# exactly one figure will be displaced per move
subto one_move_future: forall <move> in FUTURE_MOVES | move > 0:
    (sum <t,color> in CURRENT_PIECES | is_playing_at_move(move,color) == 1:
        sum <r2,c2> in BOARD:
            ((future_moves[move-1,r2,c2,t,color])*(1-future_moves[move,r2,c2,t,color]))  ) == 1;


subto pawn_move: forall <move> in FUTURE_MOVES | move > 0:
    forall <t,color> in CURRENT_PIECES | t == "PAWN" and is_playing_at_move(move,color) == 1:
        (sum <r2,c2,r3,c3> in BOARD * BOARD |  (valid_pawn_move(r2, c2, r3, c3, color) == 1 or valid_pawn_eat_move (r2, c2, r3, c3, color) == 1 or (r2 == r3 and c2 == c3)) :
            (future_moves[move-1,r2,c2,t,color] * future_moves[move,r3,c3,t,color] ) ) == 1;

subto queen_move: forall <move> in FUTURE_MOVES | move > 0:
    forall <t,color> in CURRENT_PIECES | t == "QUEEN" and is_playing_at_move(move,color) == 1:
        (sum <r2,c2,r3,c3> in BOARD * BOARD |   (valid_queen_move(r2, c2, r3, c3) == 1 or (r2 == r3 and c2 == c3)) :
            (future_moves[move-1,r2,c2,t,color] * future_moves[move,r3,c3,t,color] ) ) == 1;

subto rook_move: forall <move> in FUTURE_MOVES | move > 0:
    forall <t,color> in CURRENT_PIECES | t == "ROOK" and is_playing_at_move(move,color) == 1:
        (sum <r2,c2,r3,c3> in BOARD * BOARD |   (valid_rook_move(r2, c2, r3, c3) == 1 or (r2 == r3 and c2 == c3)) :
            (future_moves[move-1,r2,c2,t,color] * future_moves[move,r3,c3,t,color] ) ) == 1;

subto bishop_move: forall <move> in FUTURE_MOVES | move > 0:
    forall <t,color> in CURRENT_PIECES | t == "BISHOP" and is_playing_at_move(move,color) == 1:
        (sum <r2,c2,r3,c3> in BOARD * BOARD |   (valid_bishop_move(r2, c2, r3, c3) == 1 or (r2 == r3 and c2 == c3)) :
            (future_moves[move-1,r2,c2,t,color] * future_moves[move,r3,c3,t,color] ) ) == 1;

subto knight_move: forall <move> in FUTURE_MOVES | move > 0:
    forall <t,color> in CURRENT_PIECES | t == "KNIGHT" and is_playing_at_move(move,color) == 1:
        (sum <r2,c2,r3,c3> in BOARD * BOARD |   (valid_knight_move(r2, c2, r3, c3) == 1 or (r2 == r3 and c2 == c3)) :
            (future_moves[move-1,r2,c2,t,color] * future_moves[move,r3,c3,t,color] ) ) == 1;

subto king_move: forall <move> in FUTURE_MOVES | move > 0:
    forall <t,color> in CURRENT_PIECES | t == "KING" and is_playing_at_move(move,color) == 1:
            (sum <r2,c2,r3,c3> in BOARD * BOARD |   (valid_king_move(r2, c2, r3, c3) == 1 or (r2 == r3 and c2 == c3)) :
            (future_moves[move-1,r2,c2,t,color] * future_moves[move,r3,c3,t,color] ) ) == 1;

subto figure_stay: forall <move> in FUTURE_MOVES | move > 0:
    forall <t,color> in CURRENT_PIECES | is_playing_at_move(move,color) == 0:
        forall <r2,c2> in BOARD:
            future_moves[move-1,r2,c2,t,color] - future_moves[move,r2,c2,t,color] >= 0;


param pawn_cost := 1;
param knight_cost := 3;
param bishop_cost := 3;
param rook_cost := 5;
param queen_cost := 9;
param king_cost := 1000;

param decayFactor := 0.8;

maximize obj:
    sum <move,r,c,t,color> in FUTURE_MOVES * BOARD * CURRENT_PIECES | is_playing_at_move(move,color) == 1 and t == "PAWN":
        future_moves[move,r,c,t,color] * pawn_cost * decayFactor^move
    + sum <move,r,c,t,color> in FUTURE_MOVES * BOARD * CURRENT_PIECES | is_playing_at_move(move,color) == 1 and t == "KNIGHT":
        future_moves[move,r,c,t,color] * knight_cost * decayFactor^move
    + sum <move,r,c,t,color> in FUTURE_MOVES * BOARD * CURRENT_PIECES | is_playing_at_move(move,color) == 1 and t == "BISHOP":
        future_moves[move,r,c,t,color] * bishop_cost * decayFactor^move
    + sum <move,r,c,t,color> in FUTURE_MOVES * BOARD * CURRENT_PIECES | is_playing_at_move(move,color) == 1 and t == "ROOK":
        future_moves[move,r,c,t,color] * rook_cost * decayFactor^move
    + sum <move,r,c,t,color> in FUTURE_MOVES * BOARD * CURRENT_PIECES | is_playing_at_move(move,color) == 1 and t == "QUEEN":
        future_moves[move,r,c,t,color] * queen_cost * decayFactor^move
    + sum <move,r,c,t,color> in FUTURE_MOVES * BOARD * CURRENT_PIECES | is_playing_at_move(move,color) == 1 and t == "KING":
        future_moves[move,r,c,t,color] * king_cost * decayFactor^move;
        
        
        

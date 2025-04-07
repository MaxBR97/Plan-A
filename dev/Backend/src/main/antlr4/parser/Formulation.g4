/**
 * ANTLR grammar for the Zimpl language. <p/>
 *
 * This grammar allows for the distinction between (abstract)
 * Formulations, where some elements may be declared but not assigned
 * any value, and Models, where all elements are concretely defined.
 * 
 * @see <a href="https://zimpl.zib.de/download/zimpl.pdf">
 * 		[Koch2020] Zimpl User Guide </a>
 * @see <a href="https://www.antlr.org/">ANTLR</a>
 * @see <a href="https://dl.acm.org/doi/10.5555/1823613"> [Parr2009]
 * 		Language Implementation Patterns. ISBN 193435645X.</a>
 */

grammar Formulation;



/** Start rule. */
program:	formulation EOF ;

formulation : (command | statement)* ;

// TODO: test commands
command : 'do' forall* ( print | check ) ';' ;

check	: 'check' boolExpr ;

print	: 'print' csv ;

statement :
	(	set
	|	parameter
	|	variable
	|	objective
	|	constraint
	|	function
	)	';'
	;

// NOTE [design guideline]: it's generally a good idea to name rule
// fields but excessive naming bites readability; prefer naming if AST
// construction benefits from it. Eg: adding named field 'value' in
// 		set : 'set' name=ID ':=' value=setExpr	# SetDefExpr | ...
// is clear enough but does not benefit AST construction since 'setExpr'
// is readily and unambiguously available.
// Contrast this with
// 		set : 'set' name=ID					# SetDecl
// where named field 'name' improves readability AND is used in AST
// construction to extract Token from lex rule.

// TODO: indexed sets are not fully tested
set :	'set' sqRef							# SetDecl
	|	'set' sqRef ':=' setExpr			# SetDefExpr
	|	'set' sqRef ':=' '{' readfile '}'	# SetDefRead
	;

// NOTE [design guideline]: consistency is very important but sometimes
// leads to tedious repetition; prefer consistency unless AST
// construction suffers from it. Eg: defining 'parameter' subrules
// 		: 'param' sqRef						# ParamDecl
// 		| 'param' sqRef ':=' elem			# ParamDefElem ...
// is consistent with current 'set' rule def but leads to almost
// identical duplicated methods exitParamDecl() & exitParamDefElem()
// in AST construction.

parameter
	:	'param' sqRef ( ':=' expr )?		# ParamDecl
	|	'param' sqRef ':=' mapping			# ParamDef
	;

// FIXME: there's another possible 'index' & 'mapping' combination,
// using expressions such as:
//		'param x[<i> in { 1 .. 8 } with i mod 2 == 0] := 3 * i ;'

// TODO: add 'mapping' ('readfile', 'default', empty chain, 'elem'?) tests
// FIXME: visitors get ugly; should label alternatives?
mapping : ( readfile | chain | readfile ',' chain) ('default' expr)? ;

// FIXME: should empty chains be allowed? They are not in Zimpl. When
// combined with 'default' they seem single elem def; otherwise, though
// mathematically possible, it's not clear how to handle them. Review!
chain	: ( duo (',' duo)* )? ;

// TODO: add 'readfile' tests
readfile: 'read' filename=STRING 'as' template=STRING readopts*;
readopts
	:	'skip' skip=INT
	|	'use' use=INT
	|	'match' match=STRING
	|	'comment' comment=STRING
	;

variable: 'var' sqRef VAR_TYPE? b1=bound? b2=bound? ;
VAR_TYPE: 'real' | 'binary' | 'integer' ;

objective: GOAL name=ID? ':' nExpr;
GOAL	: 'minimize' | 'maximize' ;

// TODO: redo tests
// Mmmm... does empty tail recursion work properly in antlr4 ?
// constraint: 'subto' name=ID? ':' forall comparison ; // nested alt (tail recursion)
constraint: 'subto' name=ID? ':' forall* boolExpr ;

// TODO: redo tests
// Mmmm... does empty tail recursion work properly in antlr4 ?
//forall	: 'forall' condition sep=('do' | ':') forall	# ForallNested
//		|													# ForallEmpty
//		;
forall	: 'forall' condition sep=('do' | ':') ;

function: FN_TYPE fnRef (':=' expr)? ;
FN_TYPE : 'defnumb' | 'defstrg' | 'defbool' | 'defset' ;

// EXPRESSIONS:


/** numerical expression. [Koch2020] calls this a Term. */
// TODO: add tests for sign!
nExpr:	sign=('+'|'-')? uExpr ;

/** unsigned (numerical) expression */
uExpr
	:	uExpr op='**' 	   uExpr
	|	uExpr op=('*'|'/') uExpr
	|	uExpr op=('+'|'-') uExpr
	|   uExpr op='mod'   uExpr
	|	basicExpr
	;

// TODO: add tests for BasicExprIf
basicExpr
	:	token=(INT|FLOAT|INFINITY)	# BasicExprToken
	|	(fnRef | sqRef | redExpr)	# BasicExprStack
	|	'(' nExpr ')'				# BasicExprParen
	|	ifExpr						# BasicExprIf
	;

// TODO: add tests for ifExpr
setExpr
	:	setExpr op='*' setExpr				# SetExprBin
	|	setExpr op=('+'|'\\'|'-'|'union') setExpr	# SetExprBin
	|	'(' setExpr ')'						# SetExprParen
	|	(setDesc | fnRef | sqRef | ifExpr)	# SetExprStack
	;

setDesc:'{' '}'						# SetDescEmpty
	|	'{' condition ':' tuple '}' # SetDescExtended
	|	'{' condition '}' 			# SetDescStack
	|	'{' csv '}'					# SetDescStack
	|	'{' range '}'				# SetDescStack
	;

range	:	lhs=nExpr '..' rhs=nExpr ('by' step=nExpr)?;

tuple	:	'<' csv '>' ;

/* reduce expression */
// TODO: test non-sum
//sumExpr :	'sum' condition sep=('do' | ':') nExpr ;
redExpr :	op=('prod'|'sum'|'max'|'min') condition sep=('do'|':') nExpr # LongRedExpr
		|	op=('min'|'max'|'card'|'floor'|'random')	'(' ( index | csv ) ')'			 # ShortRedExpr
		| 	op='ord' '(' setExpr ',' idx=expr ',' comp=expr ')' #OrdRedExpr
		;
// Defining RED as lexical rule brings token conflict with (functions) 'min', 'max'
//redExpr :	op=ID condition sep=('do'|':') nExpr ;

index	:	condition | setExpr ;


// In Section 4.5, [Koch2020] calls this an 'index'.
condition : tuple 'in' setExpr (sep=('with' | '|') boolExpr)? ;
// membership? 'condition' seems a synonym of boolExpr, not a subtype of it

/** boolean expression */
// TODO: add more tests   
boolExpr
	:	( condition | comparison )		# BoolExprStack
	|	'not' boolExpr					# BoolExprNot
	|	boolExpr op='and' boolExpr		# BoolExprBin
	|	boolExpr op='xor' boolExpr		# BoolExprBin
	|	boolExpr op='or' boolExpr		# BoolExprBin
	|	token=('true'|'false')			# BoolExprToken
	|	'(' boolExpr ')'				# BoolExprParen
	|	ifExpr							# BoolExprStack
	|	fnRef							# BoolExprFunc
	;

// TODO: add 'bound+' & strExpr tests
comparison
	:   ifExpr							# ComparisonIfExpr
	|	nExpr bound+					# ComparisonNExpr
	|	lhs=strExpr cmp rhs=strExpr		# ComparisonStrExpr
	;

bound	:	cmp nExpr ;
// Defining CMP as lexical rule brings token conflict with <tuple>.
cmp		:	token=( '<' | '<=' | '!=' | '==' | '>=' | '>' ) ;
// Lex rule INFINITY generates associated Token type, convenient for AST
INFINITY:	'infinity' ;

// reference with (optional) square-brackets: indexed sets, params & vars
sqRef	:	name=ID '[' index ']'		# SqRefIndex
		|	name=ID ('[' csv ']')?	# SqRefCsv		// accepts 'id[]'
		;

fnRef	:	'proj' '(' setExpr ',' tuple ')' 		#ProjFunc
		|	name=ID '(' ( index | csv ) ')' 	#CustomFunc
		;	

// TODO: try using 'csv' for any comma separated list: indices, chains...
// Then, delegate to a future type checker visitor the enforcement of
// zimpl-lang compatibilitiy issues (which forbids some combinations,
// such as tuples of tuples ("< <a>, b>") or sets as function args.

csv		:	( expr (',' expr)* )? ;

duo 	:	tuple expr ;

// NOTE: 'duo' is excluded from this list as it otherwise leads to unin-
// tended parse trees in 'param', where a single 'duo' is a proper expr,
// instead of an element in a chain of mappings, and also in 'condition'
// TODO: add lots of tests
expr	:	nExpr | strExpr | boolExpr | setExpr | tuple ;

// TODO: add tests!
strExpr	:	STRING					# StrExprToken
		|	(fnRef | sqRef)			# StrExprStack
		|	strExpr op='+' strExpr	# StrExprBin
		|	ifExpr					# StrExprIf
		;

ifExpr	:	'vif' boolExpr 'then' thenExpr=expr ('else' elseExpr=expr)? 'end' #VarIfExpr
		| 	'if' boolExpr 'then' thenExpr=expr ('else' elseExpr=expr)? 'end' #RegIfExpr
		;

// LEXERRULES:

// Numbers
//---------------------------------------------------------
INT : [0-9]+ ;
FLOAT
	:	[0-9]* '.' INT Exponent?
	|	INT Exponent
	;

fragment Exponent :	ExponentIndicator Sign? [0-9]+ ;
fragment ExponentIndicator : [eE] ;
fragment Sign :	[+-] ;

// Text
//---------------------------------------------------------
STRING	:	'"' .*? '"' ;

// TODO: add tests
ID	:	StartChar IdChar* ;

fragment StartChar
	:	[a-zA-Z]
	|	[α-ωΑ-Ω]
	;
fragment IdChar
	:	StartChar
	|	[0-9]
	|	'_'
	;

// Whitespace
//---------------------------------------------------------
WS	:	[ \t\r\n]+ -> skip ;

// Comments
//---------------------------------------------------------
COMMENT_LINE : '#' .*? ('\r'? '\n' | EOF) -> channel(HIDDEN);


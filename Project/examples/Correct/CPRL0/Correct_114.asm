   PROGRAM 8
   CALL _main
   HALT
_main:
   LDCSTR "Enter value for i: "
   PUTSTR 19
   LDGADDR 0
   GETINT
   LDCSTR "Enter value for j: "
   PUTSTR 19
   LDGADDR 4
   GETINT
L0:
   LDGADDR 0
   LOADW
   LDCINT 10
   BG L1
L4:
   LDGADDR 4
   LOADW
   LDCINT 10
   BG L5
   LDGADDR 0
   LDGADDR 0
   LOADW
   LDCINT 1
   ADD
   STOREW
   LDGADDR 4
   LDGADDR 4
   LOADW
   LDCINT 2
   ADD
   STOREW
   BR L4
L5:
   LDGADDR 0
   LDGADDR 0
   LOADW
   LDCINT 3
   ADD
   STOREW
   BR L0
L1:
   LDCSTR "i = "
   PUTSTR 4
   LDGADDR 0
   LOADW
   PUTINT
   LDCSTR ", j = "
   PUTSTR 6
   LDGADDR 4
   LOADW
   PUTINT
   PUTEOL
   RET 0

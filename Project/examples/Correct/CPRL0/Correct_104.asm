   PROGRAM 8
   LDGADDR 0
   LDCINT 1
   STOREW
   LDGADDR 4
   LDCINT 3
   STOREW
   CALL _main
   HALT
_main:
   LDGADDR 0
   LOADW
   PUTINT
   PUTEOL
   LDCINT 5
   PUTINT
   PUTEOL
   LDGADDR 4
   LOADW
   PUTINT
   PUTEOL
   RET 0

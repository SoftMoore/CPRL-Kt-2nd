   PROGRAM 4
   CALL _main
   HALT
_p:
   PROC 4
   LDLADDR 8
   LDGADDR 0
   LOADW
   STOREW
   LDCSTR "n = "
   PUTSTR 4
   LDLADDR 8
   LOADW
   PUTINT
   PUTEOL
   RET 0
_main:
   LDGADDR 0
   LDCINT 5
   STOREW
   CALL _p
   RET 0

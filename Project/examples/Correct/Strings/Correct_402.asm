   CALL _main
   HALT
_main:
   PROC 16
   LDLADDR 8
   LDCSTR "John"
   STORE 12
   LDLADDR 20
   LDCINT 0
   STOREW
L0:
   LDLADDR 20
   LOADW
   LDCINT 4
   BGE L1
   LDLADDR 8
   LDCINT 4
   ADD
   LDLADDR 20
   LOADW
   LDCINT 2
   MUL
   ADD
   LOAD2B
   PUTCH
   LDLADDR 20
   LDLADDR 20
   LOADW
   LDCINT 1
   ADD
   STOREW
   BR L0
L1:
   PUTEOL
   RET 0

   CALL _main
   HALT
_main:
   PROC 24
   LDLADDR 8
   LDCSTR "John"
   STORE 12
   LDCSTR "Hello, "
   PUTSTR 7
   LDLADDR 8
   LOAD 12
   PUTSTR 4
   PUTEOL
   LDLADDR 20
   LDLADDR 8
   LOAD 12
   STORE 12
   LDLADDR 20
   LDCINT 4
   ADD
   LDCINT 2
   LDCINT 2
   MUL
   ADD
   LDCCH 'a'
   STORE2B
   LDCSTR "Hello, "
   PUTSTR 7
   LDLADDR 20
   LOAD 12
   PUTSTR 4
   PUTEOL
   RET 0

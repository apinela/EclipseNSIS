/* Generated By:JavaCC: Do not edit this line. NSISHelpSearchQueryParserTokenManager.java */
package net.sf.eclipsensis.help.search.parser;

public class NSISHelpSearchQueryParserTokenManager implements NSISHelpSearchQueryParserConstants
{
  public  java.io.PrintStream debugStream = System.out;
  public  void setDebugStream(java.io.PrintStream ds) { debugStream = ds; }
private final int jjStopAtPos(int pos, int kind)
{
   jjmatchedKind = kind;
   jjmatchedPos = pos;
   return pos + 1;
}
private final int jjMoveStringLiteralDfa0_3()
{
   switch(curChar)
   {
      case 40:
         return jjStopAtPos(0, 14);
      case 41:
         return jjStopAtPos(0, 15);
      case 43:
         return jjStopAtPos(0, 12);
      case 45:
         return jjStopAtPos(0, 13);
      case 58:
         return jjStopAtPos(0, 16);
      case 91:
         return jjStopAtPos(0, 22);
      case 94:
         return jjStopAtPos(0, 17);
      case 123:
         return jjStopAtPos(0, 23);
      default :
         return jjMoveNfa_3(2, 0);
   }
}
private final void jjCheckNAdd(int state)
{
   if (jjrounds[state] != jjround)
   {
      jjstateSet[jjnewStateCnt++] = state;
      jjrounds[state] = jjround;
   }
}
private final void jjAddStates(int start, int end)
{
   do {
      jjstateSet[jjnewStateCnt++] = jjnextStates[start];
   } while (start++ != end);
}
private final void jjCheckNAddTwoStates(int state1, int state2)
{
   jjCheckNAdd(state1);
   jjCheckNAdd(state2);
}
private final void jjCheckNAddStates(int start, int end)
{
   do {
      jjCheckNAdd(jjnextStates[start]);
   } while (start++ != end);
}
static final long[] jjbitVec0 = {
   0xfffffffffffffffeL, 0xffffffffffffffffL, 0xffffffffffffffffL, 0xffffffffffffffffL
};
static final long[] jjbitVec2 = {
   0x0L, 0x0L, 0xffffffffffffffffL, 0xffffffffffffffffL
};
private final int jjMoveNfa_3(int startState, int curPos)
{
   int startsAt = 0;
   jjnewStateCnt = 28;
   int i = 1;
   jjstateSet[0] = startState;
   int kind = 0x7fffffff;
   for (;;)
   {
      if (++jjround == 0x7fffffff) {
        ReInitRounds();
    }
      if (curChar < 64)
      {
         long l = 1L << curChar;
         do
         {
            switch(jjstateSet[--i])
            {
               case 2:
                  if ((0x7bffd0f8ffffd9ffL & l) != 0L)
                  {
                     if (kind > 19) {
                        kind = 19;
                    }
                     jjCheckNAddStates(0, 3);
                  }
                  else if (curChar == 34) {
                    jjCheckNAdd(14);
                }
                else if (curChar == 33)
                  {
                     if (kind > 11) {
                        kind = 11;
                    }
                  }
                  if (curChar == 38) {
                    jjstateSet[jjnewStateCnt++] = 3;
                }
                  break;
               case 3:
                  if (curChar == 38 && kind > 9) {
                    kind = 9;
                }
                  break;
               case 4:
                  if (curChar == 38) {
                    jjstateSet[jjnewStateCnt++] = 3;
                }
                  break;
               case 12:
                  if (curChar == 33 && kind > 11) {
                    kind = 11;
                }
                  break;
               case 13:
                  if (curChar == 34) {
                    jjCheckNAdd(14);
                }
                  break;
               case 14:
                  if ((0xfffffffbffffffffL & l) != 0L) {
                    jjCheckNAddTwoStates(14, 15);
                }
                  break;
               case 15:
                  if (curChar == 34 && kind > 18) {
                    kind = 18;
                }
                  break;
               case 17:
                  if ((0x3ff000000000000L & l) == 0L) {
                    break;
                }
                  if (kind > 20) {
                    kind = 20;
                }
                  jjAddStates(4, 5);
                  break;
               case 18:
                  if (curChar == 46) {
                    jjCheckNAdd(19);
                }
                  break;
               case 19:
                  if ((0x3ff000000000000L & l) == 0L) {
                    break;
                }
                  if (kind > 20) {
                    kind = 20;
                }
                  jjCheckNAdd(19);
                  break;
               case 20:
                  if ((0x7bffd0f8ffffd9ffL & l) == 0L) {
                    break;
                }
                  if (kind > 19) {
                    kind = 19;
                }
                  jjCheckNAddStates(0, 3);
                  break;
               case 21:
                  if ((0x7bfff8f8ffffd9ffL & l) == 0L) {
                    break;
                }
                  if (kind > 19) {
                    kind = 19;
                }
                  jjCheckNAddTwoStates(21, 22);
                  break;
               case 23:
                  if ((0x84002f0600000000L & l) == 0L) {
                    break;
                }
                  if (kind > 19) {
                    kind = 19;
                }
                  jjCheckNAddTwoStates(21, 22);
                  break;
               case 24:
                  if ((0xfbfffcf8ffffd9ffL & l) == 0L) {
                    break;
                }
                  if (kind > 21) {
                    kind = 21;
                }
                  jjCheckNAddTwoStates(24, 25);
                  break;
               case 26:
                  if ((0x84002f0600000000L & l) == 0L) {
                    break;
                }
                  if (kind > 21) {
                    kind = 21;
                }
                  jjCheckNAddTwoStates(24, 25);
                  break;
               default : break;
            }
         } while(i != startsAt);
      }
      else if (curChar < 128)
      {
         long l = 1L << (curChar & 077);
         do
         {
            switch(jjstateSet[--i])
            {
               case 2:
                  if ((0x97ffffff97ffffffL & l) != 0L)
                  {
                     if (kind > 19) {
                        kind = 19;
                    }
                     jjCheckNAddStates(0, 3);
                  }
                  else if (curChar == 126)
                  {
                     if (kind > 20) {
                        kind = 20;
                    }
                     jjstateSet[jjnewStateCnt++] = 17;
                  }
                  if (curChar == 92) {
                    jjCheckNAddTwoStates(23, 26);
                }
                else if (curChar == 78) {
                    jjstateSet[jjnewStateCnt++] = 10;
                }
                else if (curChar == 124) {
                    jjstateSet[jjnewStateCnt++] = 7;
                }
                else if (curChar == 79) {
                    jjstateSet[jjnewStateCnt++] = 5;
                }
                else if (curChar == 65) {
                    jjstateSet[jjnewStateCnt++] = 1;
                }
                  break;
               case 0:
                  if (curChar == 68 && kind > 9) {
                    kind = 9;
                }
                  break;
               case 1:
                  if (curChar == 78) {
                    jjstateSet[jjnewStateCnt++] = 0;
                }
                  break;
               case 5:
                  if (curChar == 82 && kind > 10) {
                    kind = 10;
                }
                  break;
               case 6:
                  if (curChar == 79) {
                    jjstateSet[jjnewStateCnt++] = 5;
                }
                  break;
               case 7:
                  if (curChar == 124 && kind > 10) {
                    kind = 10;
                }
                  break;
               case 8:
                  if (curChar == 124) {
                    jjstateSet[jjnewStateCnt++] = 7;
                }
                  break;
               case 9:
                  if (curChar == 84 && kind > 11) {
                    kind = 11;
                }
                  break;
               case 10:
                  if (curChar == 79) {
                    jjstateSet[jjnewStateCnt++] = 9;
                }
                  break;
               case 11:
                  if (curChar == 78) {
                    jjstateSet[jjnewStateCnt++] = 10;
                }
                  break;
               case 14:
                  jjAddStates(6, 7);
                  break;
               case 16:
                  if (curChar != 126) {
                    break;
                }
                  if (kind > 20) {
                    kind = 20;
                }
                  jjstateSet[jjnewStateCnt++] = 17;
                  break;
               case 20:
                  if ((0x97ffffff97ffffffL & l) == 0L) {
                    break;
                }
                  if (kind > 19) {
                    kind = 19;
                }
                  jjCheckNAddStates(0, 3);
                  break;
               case 21:
                  if ((0x97ffffff97ffffffL & l) == 0L) {
                    break;
                }
                  if (kind > 19) {
                    kind = 19;
                }
                  jjCheckNAddTwoStates(21, 22);
                  break;
               case 22:
                  if (curChar == 92) {
                    jjCheckNAddTwoStates(23, 23);
                }
                  break;
               case 23:
                  if ((0x6800000078000000L & l) == 0L) {
                    break;
                }
                  if (kind > 19) {
                    kind = 19;
                }
                  jjCheckNAddTwoStates(21, 22);
                  break;
               case 24:
                  if ((0x97ffffff97ffffffL & l) == 0L) {
                    break;
                }
                  if (kind > 21) {
                    kind = 21;
                }
                  jjCheckNAddTwoStates(24, 25);
                  break;
               case 25:
                  if (curChar == 92) {
                    jjCheckNAddTwoStates(26, 26);
                }
                  break;
               case 26:
                  if ((0x6800000078000000L & l) == 0L) {
                    break;
                }
                  if (kind > 21) {
                    kind = 21;
                }
                  jjCheckNAddTwoStates(24, 25);
                  break;
               case 27:
                  if (curChar == 92) {
                    jjCheckNAddTwoStates(23, 26);
                }
                  break;
               default : break;
            }
         } while(i != startsAt);
      }
      else
      {
         int hiByte = (curChar >> 8);
         int i1 = hiByte >> 6;
         long l1 = 1L << (hiByte & 077);
         int i2 = (curChar & 0xff) >> 6;
         long l2 = 1L << (curChar & 077);
         do
         {
            switch(jjstateSet[--i])
            {
               case 2:
                  if (!jjCanMove_0(hiByte, i1, i2, l1, l2)) {
                    break;
                }
                  if (kind > 19) {
                    kind = 19;
                }
                  jjCheckNAddStates(0, 3);
                  break;
               case 14:
                  if (jjCanMove_0(hiByte, i1, i2, l1, l2)) {
                    jjAddStates(6, 7);
                }
                  break;
               case 21:
                  if (!jjCanMove_0(hiByte, i1, i2, l1, l2)) {
                    break;
                }
                  if (kind > 19) {
                    kind = 19;
                }
                  jjCheckNAddTwoStates(21, 22);
                  break;
               case 24:
                  if (!jjCanMove_0(hiByte, i1, i2, l1, l2)) {
                    break;
                }
                  if (kind > 21) {
                    kind = 21;
                }
                  jjCheckNAddTwoStates(24, 25);
                  break;
               default : break;
            }
         } while(i != startsAt);
      }
      if (kind != 0x7fffffff)
      {
         jjmatchedKind = kind;
         jjmatchedPos = curPos;
         kind = 0x7fffffff;
      }
      ++curPos;
      if ((i = jjnewStateCnt) == (startsAt = 28 - (jjnewStateCnt = startsAt))) {
        return curPos;
    }
      try { curChar = input_stream.readChar(); }
      catch(java.io.IOException e) { return curPos; }
   }
}
private final int jjStopStringLiteralDfa_1(int pos, long active0)
{
   switch (pos)
   {
      case 0:
         if ((active0 & 0x20000000L) != 0L)
         {
            jjmatchedKind = 32;
            return 3;
         }
         if ((active0 & 0x1c0L) != 0L) {
            return 3;
        }
         return -1;
      default :
         return -1;
   }
}
private final int jjStartNfa_1(int pos, long active0)
{
   return jjMoveNfa_1(jjStopStringLiteralDfa_1(pos, active0), pos + 1);
}
private final int jjStartNfaWithStates_1(int pos, int kind, int state)
{
   jjmatchedKind = kind;
   jjmatchedPos = pos;
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) { return pos + 1; }
   return jjMoveNfa_1(state, pos + 1);
}
private final int jjMoveStringLiteralDfa0_1()
{
   switch(curChar)
   {
      case 9:
         return jjStartNfaWithStates_1(0, 6, 3);
      case 10:
         return jjStartNfaWithStates_1(0, 8, 3);
      case 13:
         return jjStartNfaWithStates_1(0, 7, 3);
      case 84:
         return jjMoveStringLiteralDfa1_1(0x20000000L);
      case 125:
         return jjStopAtPos(0, 30);
      default :
         return jjMoveNfa_1(0, 0);
   }
}
private final int jjMoveStringLiteralDfa1_1(long active0)
{
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      jjStopStringLiteralDfa_1(0, active0);
      return 1;
   }
   switch(curChar)
   {
      case 79:
         if ((active0 & 0x20000000L) != 0L) {
            return jjStartNfaWithStates_1(1, 29, 3);
        }
         break;
      default :
         break;
   }
   return jjStartNfa_1(0, active0);
}
private final int jjMoveNfa_1(int startState, int curPos)
{
   int startsAt = 0;
   jjnewStateCnt = 4;
   int i = 1;
   jjstateSet[0] = startState;
   int kind = 0x7fffffff;
   for (;;)
   {
      if (++jjround == 0x7fffffff) {
        ReInitRounds();
    }
      if (curChar < 64)
      {
         long l = 1L << curChar;
         do
         {
            switch(jjstateSet[--i])
            {
               case 0:
                  if ((0xfffffffeffffffffL & l) != 0L)
                  {
                     if (kind > 32) {
                        kind = 32;
                    }
                     jjCheckNAdd(3);
                  }
                  if (curChar == 34) {
                    jjCheckNAdd(1);
                }
                  break;
               case 1:
                  if ((0xfffffffbffffffffL & l) != 0L) {
                    jjCheckNAddTwoStates(1, 2);
                }
                  break;
               case 2:
                  if (curChar == 34 && kind > 31) {
                    kind = 31;
                }
                  break;
               case 3:
                  if ((0xfffffffeffffffffL & l) == 0L) {
                    break;
                }
                  if (kind > 32) {
                    kind = 32;
                }
                  jjCheckNAdd(3);
                  break;
               default : break;
            }
         } while(i != startsAt);
      }
      else if (curChar < 128)
      {
         long l = 1L << (curChar & 077);
         do
         {
            switch(jjstateSet[--i])
            {
               case 0:
               case 3:
                  if ((0xdfffffffffffffffL & l) == 0L) {
                    break;
                }
                  if (kind > 32) {
                    kind = 32;
                }
                  jjCheckNAdd(3);
                  break;
               case 1:
                  jjAddStates(8, 9);
                  break;
               default : break;
            }
         } while(i != startsAt);
      }
      else
      {
         int hiByte = (curChar >> 8);
         int i1 = hiByte >> 6;
         long l1 = 1L << (hiByte & 077);
         int i2 = (curChar & 0xff) >> 6;
         long l2 = 1L << (curChar & 077);
         do
         {
            switch(jjstateSet[--i])
            {
               case 0:
               case 3:
                  if (!jjCanMove_0(hiByte, i1, i2, l1, l2)) {
                    break;
                }
                  if (kind > 32) {
                    kind = 32;
                }
                  jjCheckNAdd(3);
                  break;
               case 1:
                  if (jjCanMove_0(hiByte, i1, i2, l1, l2)) {
                    jjAddStates(8, 9);
                }
                  break;
               default : break;
            }
         } while(i != startsAt);
      }
      if (kind != 0x7fffffff)
      {
         jjmatchedKind = kind;
         jjmatchedPos = curPos;
         kind = 0x7fffffff;
      }
      ++curPos;
      if ((i = jjnewStateCnt) == (startsAt = 4 - (jjnewStateCnt = startsAt))) {
        return curPos;
    }
      try { curChar = input_stream.readChar(); }
      catch(java.io.IOException e) { return curPos; }
   }
}
private final int jjMoveStringLiteralDfa0_0()
{
   return jjMoveNfa_0(0, 0);
}
private final int jjMoveNfa_0(int startState, int curPos)
{
   int startsAt = 0;
   jjnewStateCnt = 3;
   int i = 1;
   jjstateSet[0] = startState;
   int kind = 0x7fffffff;
   for (;;)
   {
      if (++jjround == 0x7fffffff) {
        ReInitRounds();
    }
      if (curChar < 64)
      {
         long l = 1L << curChar;
         do
         {
            switch(jjstateSet[--i])
            {
               case 0:
                  if ((0x3ff000000000000L & l) == 0L) {
                    break;
                }
                  if (kind > 24) {
                    kind = 24;
                }
                  jjAddStates(10, 11);
                  break;
               case 1:
                  if (curChar == 46) {
                    jjCheckNAdd(2);
                }
                  break;
               case 2:
                  if ((0x3ff000000000000L & l) == 0L) {
                    break;
                }
                  if (kind > 24) {
                    kind = 24;
                }
                  jjCheckNAdd(2);
                  break;
               default : break;
            }
         } while(i != startsAt);
      }
      else if (curChar < 128)
      {
         do
         {
            switch(jjstateSet[--i])
            {
               default : break;
            }
         } while(i != startsAt);
      }
      else
      {
         do
         {
            switch(jjstateSet[--i])
            {
               default : break;
            }
         } while(i != startsAt);
      }
      if (kind != 0x7fffffff)
      {
         jjmatchedKind = kind;
         jjmatchedPos = curPos;
         kind = 0x7fffffff;
      }
      ++curPos;
      if ((i = jjnewStateCnt) == (startsAt = 3 - (jjnewStateCnt = startsAt))) {
        return curPos;
    }
      try { curChar = input_stream.readChar(); }
      catch(java.io.IOException e) { return curPos; }
   }
}
private final int jjStopStringLiteralDfa_2(int pos, long active0)
{
   switch (pos)
   {
      case 0:
         if ((active0 & 0x2000000L) != 0L)
         {
            jjmatchedKind = 28;
            return 3;
         }
         if ((active0 & 0x1c0L) != 0L) {
            return 3;
        }
         return -1;
      default :
         return -1;
   }
}
private final int jjStartNfa_2(int pos, long active0)
{
   return jjMoveNfa_2(jjStopStringLiteralDfa_2(pos, active0), pos + 1);
}
private final int jjStartNfaWithStates_2(int pos, int kind, int state)
{
   jjmatchedKind = kind;
   jjmatchedPos = pos;
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) { return pos + 1; }
   return jjMoveNfa_2(state, pos + 1);
}
private final int jjMoveStringLiteralDfa0_2()
{
   switch(curChar)
   {
      case 9:
         return jjStartNfaWithStates_2(0, 6, 3);
      case 10:
         return jjStartNfaWithStates_2(0, 8, 3);
      case 13:
         return jjStartNfaWithStates_2(0, 7, 3);
      case 84:
         return jjMoveStringLiteralDfa1_2(0x2000000L);
      case 93:
         return jjStopAtPos(0, 26);
      default :
         return jjMoveNfa_2(0, 0);
   }
}
private final int jjMoveStringLiteralDfa1_2(long active0)
{
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      jjStopStringLiteralDfa_2(0, active0);
      return 1;
   }
   switch(curChar)
   {
      case 79:
         if ((active0 & 0x2000000L) != 0L) {
            return jjStartNfaWithStates_2(1, 25, 3);
        }
         break;
      default :
         break;
   }
   return jjStartNfa_2(0, active0);
}
private final int jjMoveNfa_2(int startState, int curPos)
{
   int startsAt = 0;
   jjnewStateCnt = 4;
   int i = 1;
   jjstateSet[0] = startState;
   int kind = 0x7fffffff;
   for (;;)
   {
      if (++jjround == 0x7fffffff) {
        ReInitRounds();
    }
      if (curChar < 64)
      {
         long l = 1L << curChar;
         do
         {
            switch(jjstateSet[--i])
            {
               case 0:
                  if ((0xfffffffeffffffffL & l) != 0L)
                  {
                     if (kind > 28) {
                        kind = 28;
                    }
                     jjCheckNAdd(3);
                  }
                  if (curChar == 34) {
                    jjCheckNAdd(1);
                }
                  break;
               case 1:
                  if ((0xfffffffbffffffffL & l) != 0L) {
                    jjCheckNAddTwoStates(1, 2);
                }
                  break;
               case 2:
                  if (curChar == 34 && kind > 27) {
                    kind = 27;
                }
                  break;
               case 3:
                  if ((0xfffffffeffffffffL & l) == 0L) {
                    break;
                }
                  if (kind > 28) {
                    kind = 28;
                }
                  jjCheckNAdd(3);
                  break;
               default : break;
            }
         } while(i != startsAt);
      }
      else if (curChar < 128)
      {
         long l = 1L << (curChar & 077);
         do
         {
            switch(jjstateSet[--i])
            {
               case 0:
               case 3:
                  if ((0xffffffffdfffffffL & l) == 0L) {
                    break;
                }
                  if (kind > 28) {
                    kind = 28;
                }
                  jjCheckNAdd(3);
                  break;
               case 1:
                  jjAddStates(8, 9);
                  break;
               default : break;
            }
         } while(i != startsAt);
      }
      else
      {
         int hiByte = (curChar >> 8);
         int i1 = hiByte >> 6;
         long l1 = 1L << (hiByte & 077);
         int i2 = (curChar & 0xff) >> 6;
         long l2 = 1L << (curChar & 077);
         do
         {
            switch(jjstateSet[--i])
            {
               case 0:
               case 3:
                  if (!jjCanMove_0(hiByte, i1, i2, l1, l2)) {
                    break;
                }
                  if (kind > 28) {
                    kind = 28;
                }
                  jjCheckNAdd(3);
                  break;
               case 1:
                  if (jjCanMove_0(hiByte, i1, i2, l1, l2)) {
                    jjAddStates(8, 9);
                }
                  break;
               default : break;
            }
         } while(i != startsAt);
      }
      if (kind != 0x7fffffff)
      {
         jjmatchedKind = kind;
         jjmatchedPos = curPos;
         kind = 0x7fffffff;
      }
      ++curPos;
      if ((i = jjnewStateCnt) == (startsAt = 4 - (jjnewStateCnt = startsAt))) {
        return curPos;
    }
      try { curChar = input_stream.readChar(); }
      catch(java.io.IOException e) { return curPos; }
   }
}
static final int[] jjnextStates = {
   21, 24, 25, 22, 17, 18, 14, 15, 1, 2, 0, 1,
};
private static final boolean jjCanMove_0(int hiByte, int i1, int i2, long l1, long l2)
{
   switch(hiByte)
   {
      case 0:
         return ((jjbitVec2[i2] & l2) != 0L);
      default :
         if ((jjbitVec0[i1] & l1) != 0L) {
            return true;
        }
         return false;
   }
}
public static final String[] jjstrLiteralImages = {
"", null, null, null, null, null, null, null, null, null, null, null, "\53",  //$NON-NLS-1$ //$NON-NLS-2$
"\55", "\50", "\51", "\72", "\136", null, null, null, null, "\133", "\173", null,  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
"\124\117", "\135", null, null, "\124\117", "\175", null, null, }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
public static final String[] lexStateNames = {
   "Boost",  //$NON-NLS-1$
   "RangeEx",  //$NON-NLS-1$
   "RangeIn",  //$NON-NLS-1$
   "DEFAULT",  //$NON-NLS-1$
};
public static final int[] jjnewLexState = {
   -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 0, -1, -1, -1, -1, 2, 1, 3,
   -1, 3, -1, -1, -1, 3, -1, -1,
};
static final long[] jjtoToken = {
   0x1fffffe01L,
};
static final long[] jjtoSkip = {
   0x1e0L,
};
protected JavaCharStream input_stream;
private final int[] jjrounds = new int[28];
private final int[] jjstateSet = new int[56];
protected char curChar;
public NSISHelpSearchQueryParserTokenManager(JavaCharStream stream){
   if (JavaCharStream.staticFlag) {
    throw new Error("ERROR: Cannot use a static CharStream class with a non-static lexical analyzer."); //$NON-NLS-1$
}
   input_stream = stream;
}
public NSISHelpSearchQueryParserTokenManager(JavaCharStream stream, int lexState){
   this(stream);
   SwitchTo(lexState);
}
public void ReInit(JavaCharStream stream)
{
   jjmatchedPos = jjnewStateCnt = 0;
   curLexState = defaultLexState;
   input_stream = stream;
   ReInitRounds();
}
private final void ReInitRounds()
{
   int i;
   jjround = 0x80000001;
   for (i = 28; i-- > 0;) {
    jjrounds[i] = 0x80000000;
}
}
public void ReInit(JavaCharStream stream, int lexState)
{
   ReInit(stream);
   SwitchTo(lexState);
}
public void SwitchTo(int lexState)
{
   if (lexState >= 4 || lexState < 0) {
    throw new TokenMgrError("Error: Ignoring invalid lexical state : " + lexState + ". State unchanged.", TokenMgrError.INVALID_LEXICAL_STATE); //$NON-NLS-1$ //$NON-NLS-2$
}
else {
    curLexState = lexState;
}
}

protected Token jjFillToken()
{
   Token t = Token.newToken(jjmatchedKind);
   t.kind = jjmatchedKind;
   String im = jjstrLiteralImages[jjmatchedKind];
   t.image = (im == null) ? input_stream.GetImage() : im;
   t.beginLine = input_stream.getBeginLine();
   t.beginColumn = input_stream.getBeginColumn();
   t.endLine = input_stream.getEndLine();
   t.endColumn = input_stream.getEndColumn();
   return t;
}

int curLexState = 3;
int defaultLexState = 3;
int jjnewStateCnt;
int jjround;
int jjmatchedPos;
int jjmatchedKind;

public Token getNextToken()
{
  Token matchedToken;
  int curPos = 0;

  EOFLoop :
  for (;;)
  {
   try
   {
      curChar = input_stream.BeginToken();
   }
   catch(java.io.IOException e)
   {
      jjmatchedKind = 0;
      matchedToken = jjFillToken();
      return matchedToken;
   }

   switch(curLexState)
   {
     case 0:
       jjmatchedKind = 0x7fffffff;
       jjmatchedPos = 0;
       curPos = jjMoveStringLiteralDfa0_0();
       break;
     case 1:
       try { input_stream.backup(0);
          while (curChar <= 32 && (0x100000000L & (1L << curChar)) != 0L) {
            curChar = input_stream.BeginToken();
        }
       }
       catch (java.io.IOException e1) { continue EOFLoop; }
       jjmatchedKind = 0x7fffffff;
       jjmatchedPos = 0;
       curPos = jjMoveStringLiteralDfa0_1();
       break;
     case 2:
       try { input_stream.backup(0);
          while (curChar <= 32 && (0x100000000L & (1L << curChar)) != 0L) {
            curChar = input_stream.BeginToken();
        }
       }
       catch (java.io.IOException e1) { continue EOFLoop; }
       jjmatchedKind = 0x7fffffff;
       jjmatchedPos = 0;
       curPos = jjMoveStringLiteralDfa0_2();
       break;
     case 3:
       try { input_stream.backup(0);
          while (curChar <= 32 && (0x100002600L & (1L << curChar)) != 0L) {
            curChar = input_stream.BeginToken();
        }
       }
       catch (java.io.IOException e1) { continue EOFLoop; }
       jjmatchedKind = 0x7fffffff;
       jjmatchedPos = 0;
       curPos = jjMoveStringLiteralDfa0_3();
       break;
   }
     if (jjmatchedKind != 0x7fffffff)
     {
        if (jjmatchedPos + 1 < curPos) {
            input_stream.backup(curPos - jjmatchedPos - 1);
        }
        if ((jjtoToken[jjmatchedKind >> 6] & (1L << (jjmatchedKind & 077))) != 0L)
        {
           matchedToken = jjFillToken();
       if (jjnewLexState[jjmatchedKind] != -1) {
        curLexState = jjnewLexState[jjmatchedKind];
    }
           return matchedToken;
        }
        else
        {
         if (jjnewLexState[jjmatchedKind] != -1) {
            curLexState = jjnewLexState[jjmatchedKind];
        }
           continue EOFLoop;
        }
     }
     int error_line = input_stream.getEndLine();
     int error_column = input_stream.getEndColumn();
     String error_after = null;
     boolean EOFSeen = false;
     try { input_stream.readChar(); input_stream.backup(1); }
     catch (java.io.IOException e1) {
        EOFSeen = true;
        error_after = curPos <= 1 ? "" : input_stream.GetImage(); //$NON-NLS-1$
        if (curChar == '\n' || curChar == '\r') {
           error_line++;
           error_column = 0;
        }
        else {
            error_column++;
        }
     }
     if (!EOFSeen) {
        input_stream.backup(1);
        error_after = curPos <= 1 ? "" : input_stream.GetImage(); //$NON-NLS-1$
     }
     throw new TokenMgrError(EOFSeen, curLexState, error_line, error_column, error_after, curChar, TokenMgrError.LEXICAL_ERROR);
  }
}

}

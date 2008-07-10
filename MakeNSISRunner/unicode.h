#ifndef _Included_unicode
#define _Included_unicode

#ifdef UNICODE

#define GetJavaStringChars(x,y) x->GetStringChars(y,0);
#define ReleaseJavaStringChars(x,y,z) x->ReleaseStringChars(y, z)
#define NewJavaString(x,y) x->NewString(y, _tcslen(y))

#else

#define GetJavaStringChars(x,y) x->GetStringUTFChars(y,0);
#define ReleaseJavaStringChars(x,y,z) x->ReleaseStringUTFChars(y, z)
#define NewJavaString(x,y) x->NewStringUTF(y)

#endif

#endif

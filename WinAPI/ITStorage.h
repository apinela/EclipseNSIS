/*********************************************************************************
 * Copyright (c) 2003 Christian Ernst Rysgaard (Htmlhelp Forensics - 
 * http://www.codeproject.com/winhelp/htmlhelp.asp)
 *
 *********************************************************************************/
#ifndef ITSTORAGE_H
#define ITSTORAGE_H
#if _MSC_VER > 1000
#pragma once
#endif // _MSC_VER > 1000


#include <windows.h>
#include <stdio.h>
#include <assert.h>
#pragma warning(disable: 4786)
#include <map>
using namespace std;

// --------------------------------------------------------------------------------------------------
// secret ITStorage interface from Microsoft

DEFINE_GUID(CLSID_ITStorage, 0x5d02926a, 0x212e, 0x11d0, 0x9d, 0xf9, 0x0, 0xa0, 0xc9, 0x22, 0xe6, 0xec);
DEFINE_GUID(IID_ITStorage, 0x88cc31de, 0x27ab, 0x11d0, 0x9d, 0xf9, 0x0, 0xa0, 0xc9, 0x22, 0xe6, 0xec);

typedef struct _ITS_Control_Data
{
    UINT cdwControlData;
    UINT adwControlData[1];
} ITS_Control_Data, *PITS_Control_Data;  

typedef enum ECompactionLev 
{ 
    COMPACT_DATA = 0, 
    COMPACT_DATA_AND_PATH
};

DECLARE_INTERFACE_(IITStorage, IUnknown)
{
    STDMETHOD(StgCreateDocfile) 
        (const WCHAR* pwcsName, DWORD grfMode, DWORD reserved, IStorage** ppstgOpen) PURE;
    STDMETHOD(StgCreateDocfileOnILockBytes) 
        (ILockBytes * plkbyt, DWORD grfMode, DWORD reserved, IStorage ** ppstgOpen) PURE;
    STDMETHOD(StgIsStorageFile) 
        (const WCHAR * pwcsName) PURE;
    STDMETHOD(StgIsStorageILockBytes) 
        (ILockBytes * plkbyt) PURE;
    STDMETHOD(StgOpenStorage) 
        (const WCHAR * pwcsName, IStorage * pstgPriority, DWORD grfMode, SNB snbExclude, DWORD reserved, IStorage ** ppstgOpen) PURE;
    STDMETHOD(StgOpenStorageOnILockBytes)
        (ILockBytes * plkbyt, IStorage * pStgPriority, DWORD grfMode, SNB snbExclude, DWORD reserved, IStorage ** ppstgOpen ) PURE;
    STDMETHOD(StgSetTimes)
        (WCHAR const * lpszName,  FILETIME const * pctime, FILETIME const * patime, FILETIME const * pmtime) PURE;
    STDMETHOD(SetControlData)
        (PITS_Control_Data pControlData) PURE;
    STDMETHOD(DefaultControlData)
        (PITS_Control_Data *ppControlData) PURE;
    STDMETHOD(Compact)
        (const WCHAR* pwcsName, ECompactionLev iLev) PURE;
};

// --------------------------------------------------------------------------------------------------

typedef map <ULONG, LPCSTR, less<ULONG>, allocator<LPCSTR> > tMapIntString;

class CItsFile
{
public:
    CItsFile();
    ~CItsFile();
    HRESULT OpenITS(PWCHAR pszFile);
    HRESULT OpenStorage(PWCHAR pwzName, IStorage** ppStorage, DWORD dwAccess = STGM_READWRITE);
    HRESULT OpenStream(PWCHAR pwzFile, IStream** ppStream, DWORD dwAccess = STGM_READWRITE);
        HRESULT ReadStream(PWCHAR pwzStream, ULONG* cbSize, void** pBuffer);
        IStorage* pStorage() { return m_pStorage; };

protected:
    IITStorage* m_pITStorage;
    IStorage* m_pStorage;
};


LPSTR GetErr(DWORD dwLastError);
HRESULT ExtractHtmlHelpAndTOC(LPCSTR pszFile, LPCSTR pszFolder, LPSTR tocFile);

#endif

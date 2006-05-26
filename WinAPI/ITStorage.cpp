/*********************************************************************************
 * Copyright (c) 2003 Christian Ernst Rysgaard (Htmlhelp Forensics - 
 * http://www.codeproject.com/winhelp/htmlhelp.asp)
 *
 * This is a modified version of the code downloadable from the above URL.
 * - IcemanK (Sunil Kamath)
 *
 *********************************************************************************/
#include "ITStorage.h"
#include <tchar.h>

#define READ_BLOCK_SIZE    16384

static const GUID CLSID_ITStorage = { 0x5d02926a, 0x212e, 0x11d0, { 0x9d, 0xf9, 0x0, 0xa0, 0xc9, 0x22, 0xe6, 0xec } };
static const GUID IID_ITStorage = { 0x88cc31de, 0x27ab, 0x11d0, { 0x9d, 0xf9, 0x0, 0xa0, 0xc9, 0x22, 0xe6, 0xec} };

static WCHAR* supported_extensions[] = { L".html", L".htm", L".css", L".js", L".bmp", L".jpg", L".gif", NULL };

CItsFile::CItsFile()
{
    m_pITStorage = NULL;
    m_pStorage = NULL;
}

CItsFile::~CItsFile()
{
    if (m_pStorage) {
        m_pStorage->Release();
    }
    if (m_pITStorage) {
        m_pITStorage->Release();
    }
}

HRESULT CItsFile::OpenITS(PWCHAR pwzFile) 
{
    HRESULT hr;
    hr = CoCreateInstance(CLSID_ITStorage, NULL, CLSCTX_INPROC_SERVER, IID_ITStorage, (void **) &m_pITStorage);
    if (FAILED(hr)) {
        return hr;
    }
    hr = m_pITStorage->StgOpenStorage(pwzFile, NULL, STGM_READ | STGM_SHARE_DENY_WRITE, NULL, 0, &m_pStorage);
    return hr;
}


HRESULT CItsFile::OpenStorage(PWCHAR pwzName, IStorage** ppStorage, DWORD dwAccess) 
{
    return m_pStorage->OpenStorage(pwzName, NULL, dwAccess, 0, 0, ppStorage); 
}

HRESULT CItsFile::OpenStream(PWCHAR pwzFile, IStream** ppStream, DWORD dwAccess) 
{
    return m_pStorage->OpenStream(pwzFile, NULL, dwAccess, 0, ppStream); 
}

HRESULT CItsFile::ReadStream(PWCHAR pwzStream, ULONG* cbRetSize, void** pRetBuffer)
{
    IStream* pStream = NULL;
    STATSTG statstg;
    ULONG cbActRead = 0;
    ULONG cbSize = 0;
    LPVOID pBuffer = NULL;
    HRESULT hr;

    // read entire #STRINGS into local mem
    hr = m_pStorage->OpenStream(pwzStream, NULL, STGM_READ, 0, &pStream); 
    if (FAILED(hr))
        return hr;
    hr = pStream->Stat(&statstg, STATFLAG_NONAME);
    if (FAILED(hr)) {
        pStream->Release();
        return hr;
    }

    cbSize = (ULONG) statstg.cbSize.QuadPart;
    pBuffer = (void*) LocalAlloc(LPTR, cbSize);

    hr = pStream->Read(pBuffer, cbSize, &cbActRead);
    if (FAILED(hr) || (cbActRead != cbSize)) {
        pStream->Release();
        LocalFree(pBuffer);
        return hr;
    }

    *cbRetSize = cbSize;
    *pRetBuffer = pBuffer;
    pStream->Release();
    return S_OK;
}

char* GetErr(DWORD dwLastError)
{
    LPSTR MessageBuffer;
    DWORD dwBufferLength;
    dwBufferLength = FormatMessageA(
        FORMAT_MESSAGE_ALLOCATE_BUFFER | FORMAT_MESSAGE_IGNORE_INSERTS | FORMAT_MESSAGE_FROM_SYSTEM, 
        NULL, dwLastError, MAKELANGID(LANG_NEUTRAL, SUBLANG_DEFAULT), (LPSTR) &MessageBuffer, 0, NULL);
    return MessageBuffer;
}

HRESULT SaveSub(IStorage* p, WCHAR* pwzSubstream, LPCSTR pszFilename)
{
    char t[READ_BLOCK_SIZE] = {0};
    ULONG cbRead=READ_BLOCK_SIZE;
    ULONG cbWrote=0;
    IStream* ps = NULL;
    HRESULT hr;

    hr = p->OpenStream(pwzSubstream, NULL, STGM_READ, 0, &ps);
    if (FAILED(hr)) {
        return hr;
    }

    FILE* fp = fopen(pszFilename, "w");
    if (fp==NULL) {
        DWORD err = GetLastError();
        char *p = GetErr(err);
        sprintf(t, "unable to create file %s\n%u - %s\n", pszFilename, err, p);
        OutputDebugStringA(t);
        LocalFree(p);
        return S_FALSE;
    }

    hr = S_OK;
    while (SUCCEEDED(hr) && cbRead==READ_BLOCK_SIZE) {
        hr = ps->Read(t, READ_BLOCK_SIZE, &cbRead);
        if (SUCCEEDED(hr)) {
            cbWrote = fwrite(t, sizeof(char), cbRead, fp);
            if (cbWrote!=cbRead) {
                hr = S_FALSE;
            }
        }
    }
    fclose(fp);
    ps->Release();

    return hr;
}


HRESULT ExtractHtmlHelpFromStorage(IStorage *ps, LPCSTR folder, LPSTR tocFile, LPSTR indexFile)
{
    IStorage*    psub = NULL;
    IEnumSTATSTG* pEnum = NULL;
    STATSTG entry = {0};
    char buf[4096] = {0};
    HRESULT hr = S_OK;
    LPCSTR typnam[] = { "STGTY_STORAGE", "STGTY_STREAM", "STGTY_LOCKBYTES", "STGTY_PROPERTY" };
    char newFile[MAX_PATH] = {0};
    
    // create the folder - unless it already exists
    if (!CreateDirectoryA(folder, NULL)) {
        DWORD err = GetLastError();
        if (err!=ERROR_ALREADY_EXISTS) {
            LPSTR perr = GetErr(err);
            sprintf(buf, "CreateDirectory failed - %u : %s\n", err, perr); 
            OutputDebugStringA(buf);
            LocalFree(perr);
            return S_FALSE;
        }
    }
    
    hr = ps->EnumElements(0, NULL, 0, &pEnum);
    if (FAILED(hr)) {
        OutputDebugStringA("Unable to enumerate on storage elements");
        return hr;
    }

    hr = S_OK;
    while (pEnum->Next(1, &entry, NULL)==S_OK) {
        if (entry.type == STGTY_STREAM) {
            WCHAR *pExt = wcsrchr(entry.pwcsName,'.');
            if(pExt) {
                if(tocFile && !wcsicmp(pExt,L".hhc")) {
                    sprintf(tocFile,"%s\\%S",folder,entry.pwcsName);
                    hr = SaveSub(ps, entry.pwcsName, tocFile);
                }
                else if(indexFile && !wcsicmp(pExt,L".hhk")) {
                    sprintf(indexFile,"%s\\%S",folder,entry.pwcsName);
                    hr = SaveSub(ps, entry.pwcsName, indexFile);
                }
                else {
                    int i =0;
                    while(supported_extensions[i]) {
                        if(!wcsicmp(pExt,supported_extensions[i])) {
                            sprintf(newFile,"%s\\%S",folder,entry.pwcsName);
                            hr = SaveSub(ps, entry.pwcsName, newFile);
                            break;
                        }

                        i++;
                    }
                }
            }
        } 
        else if (entry.type == STGTY_STORAGE) {
            hr = ps->OpenStorage(entry.pwcsName, NULL, STGM_READ, NULL, 0, &psub );
            if (FAILED(hr)) {
                break;
            }

            sprintf(newFile,"%s\\%S",folder,entry.pwcsName);
            hr = ExtractHtmlHelpFromStorage(psub, newFile, NULL, NULL);
            psub->Release();
            if (FAILED(hr)) {
                break;
            }
        }
    }
    pEnum->Release();

    return hr;    
}

HRESULT ExtractHtmlHelp(LPCWSTR pszFile, LPCSTR pszFolder, LPSTR tocFile, LPSTR indexFile)
{
    CItsFile itf;
    HRESULT hr;

    hr = itf.OpenITS((LPWSTR)pszFile);
    if (FAILED(hr)) {
        return hr;
    }

    hr = ExtractHtmlHelpFromStorage(itf.pStorage(), pszFolder, tocFile, indexFile);
    if (FAILED(hr)) {
        return hr;
    }

    return S_OK;
}

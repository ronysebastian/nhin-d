﻿/* 
 Copyright (c) 2010, NHIN Direct Project
 All rights reserved.

 Authors:
    Umesh Madan     umeshma@microsoft.com
  
Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
Neither the name of the The NHIN Direct Project (nhindirect.org). nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 
*/
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Security.Cryptography.X509Certificates;
using NHINDirect.Config.Store;
using NHINDirect.Config.Client.CertificateService;

namespace NHINDirect.Config.Client.CertificateService
{
    public static class CertificateExtensions
    {
        static CertificateGetOptions GetFullCertData = new CertificateGetOptions
        {
            IncludeData = true,
            IncludePrivateKey = true
        };
        
        public static void AddCertificate(this CertificateStoreClient client, Certificate cert)
        {
            if (cert == null)
            {
                throw new ArgumentNullException();  
            }
            
            client.AddCertificates(new Certificate[] {cert});
        }
        
        public static Certificate GetCertificate(this CertificateStoreClient client, long certificateID, CertificateGetOptions options)
        {
            Certificate[] certs = client.GetCertificates(new long[] {certificateID}, options);
            if (certs.IsNullOrEmpty())
            {
                return null;
            }
            
            return certs[0];
        }
        
        public static Certificate[] GetCertificatesForOwner(this CertificateStoreClient client, string owner)
        {
            return client.GetCertificatesForOwner(owner, GetFullCertData);
        }
        
        public static X509Certificate2Collection GetX509CertificatesForOwner(this CertificateStoreClient client, string owner)
        {
            return Certificate.ToX509Collection(client.GetCertificatesForOwner(owner));
        }
        
        public static IEnumerable<Certificate> EnumerateCertificates(this CertificateStoreClient client, int chunkSize)
        {
            return client.EnumerateCertificates(chunkSize, GetFullCertData);
        }

        public static IEnumerable<Certificate> EnumerateCertificates(this CertificateStoreClient client, int chunkSize, CertificateGetOptions options)
        {
            if (chunkSize <= 0 || options == null)
            {
                throw new ArgumentException();
            }

            long lastID = -1;

            Certificate[] certs;
            while (true)
            {
                certs = client.EnumerateCertificates(lastID, chunkSize, options);
                if (certs.IsNullOrEmpty())
                {
                    yield break;
                }
                
                for (int i = 0; i < certs.Length; ++i)
                {
                    yield return certs[i];
                }
                lastID = certs[certs.Length - 1].ID;
            }
        }
        
        public static IEnumerable<X509Certificate2> EnumerateX509Certificates(this CertificateStoreClient client, int chunkSize)
        {
            foreach(Certificate cert in client.EnumerateCertificates(chunkSize, GetFullCertData))
            {
                yield return cert.ToX509Certificate();
            }
        }
    }
}
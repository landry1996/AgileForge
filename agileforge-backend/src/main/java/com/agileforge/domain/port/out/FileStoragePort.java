package com.agileforge.domain.port.out;

import java.io.InputStream;

public interface FileStoragePort {

    String store(String fileName, String contentType, InputStream inputStream);

    void delete(String storagePath);

    String getUrl(String storagePath);
}

package com.pe.allpafood.api.transaction.notification.entity.email;

import java.util.ArrayList;
import java.util.List;

public record EmailAttachment(
        String fileName,
        byte[] content,
        String contentType
) { }


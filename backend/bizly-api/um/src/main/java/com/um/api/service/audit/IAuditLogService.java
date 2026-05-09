package com.um.api.service.audit;

import com.um.api.dto.audit.GetsAuditLogsRequest;
import com.um.common.PageResponse;
import com.um.api.dto.audit.AuditLogRowResponse;

public interface IAuditLogService {

	PageResponse<AuditLogRowResponse> search(GetsAuditLogsRequest request);
}

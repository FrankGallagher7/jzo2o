package com.jzo2o.customer.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jzo2o.common.model.PageResult;
import com.jzo2o.customer.model.domain.AgencyCertificationAudit;
import com.jzo2o.customer.model.domain.WorkerCertificationAudit;
import com.jzo2o.customer.model.dto.request.AgencyCertificationAuditPageQueryReqDTO;
import com.jzo2o.customer.model.dto.request.CertificationAuditReqDTO;
import com.jzo2o.customer.model.dto.request.WorkerCertificationAuditPageQueryReqDTO;

/**
 * <p>
 * 服务人员认证信息表 服务类
 * </p>
 *
 * @author itcast
 * @since 2023-09-06
 */
public interface IAgencyCertificationAuditService extends IService<AgencyCertificationAudit> {

    /**
     * 审核服务机构认证分页查询
     * @return
     */
    PageResult<AgencyCertificationAudit> pageQuery(AgencyCertificationAuditPageQueryReqDTO dto);

    /**
     * 审核服务人员认证
     * @param id
     * @param certificationAuditReqDTO
     */
    void audit(Long id, CertificationAuditReqDTO certificationAuditReqDTO);
}

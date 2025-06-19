package com.jzo2o.customer.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jzo2o.customer.model.domain.AgencyCertification;
import com.jzo2o.customer.model.dto.AgencyCertificationUpdateDTO;
import com.jzo2o.customer.model.dto.request.AgencyCertificationAuditAddReqDTO;
import com.jzo2o.customer.model.dto.request.CertificationAuditReqDTO;
import com.jzo2o.customer.model.dto.request.WorkerCertificationAuditAddReqDTO;
import com.jzo2o.customer.model.dto.response.AgencyCertificationResDTO;

/**
 * <p>
 * 机构认证信息表 服务类
 * </p>
 *
 * @author itcast
 * @since 2023-09-06
 */
public interface IAgencyCertificationService extends IService<AgencyCertification> {


    /**
     * 根据机构id更新
     *
     * @param agencyCertificationUpdateDTO 机构认证更新模型
     */
    void updateByServeProviderId(AgencyCertificationUpdateDTO agencyCertificationUpdateDTO);


    /**
     * pc新增认证申请
     */
    void postCertification(AgencyCertificationAuditAddReqDTO agencyCertificationAuditAddReqDTO);

    /**
     * pc获取拒绝原因
     * @return
     */
    CertificationAuditReqDTO getRejectReason();
}

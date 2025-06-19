package com.jzo2o.customer.controller.operation;

import com.jzo2o.common.model.PageResult;
import com.jzo2o.customer.model.domain.AgencyCertificationAudit;
import com.jzo2o.customer.model.domain.WorkerCertificationAudit;
import com.jzo2o.customer.model.dto.request.AgencyCertificationAuditPageQueryReqDTO;
import com.jzo2o.customer.model.dto.request.CertificationAuditReqDTO;
import com.jzo2o.customer.model.dto.request.WorkerCertificationAuditPageQueryReqDTO;
import com.jzo2o.customer.service.IAgencyCertificationAuditService;
import com.jzo2o.customer.service.IAgencyCertificationService;
import com.jzo2o.customer.service.IWorkerCertificationAuditService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author itcast
 */
@RestController("agencyCertificationAuditController")
@RequestMapping("/operation/agency-certification-audit")
@Api(tags = "运营端 - 服务机构审核相关接口")
public class AgencyCertificationAuditController {
    @Autowired
    private IAgencyCertificationAuditService agencyCertificationAuditService;

    @GetMapping("/page")
    @ApiOperation("审核服务机构认证分页查询")
    public PageResult<AgencyCertificationAudit> pageQuery(AgencyCertificationAuditPageQueryReqDTO dto) {
        return agencyCertificationAuditService.pageQuery(dto);
    }

    @PutMapping("/audit/{id}")
    @ApiOperation("审核服务机构认证")
    public void audit(@PathVariable Long id, @RequestBody CertificationAuditReqDTO certificationAuditReqDTO) {
        agencyCertificationAuditService.audit(id, certificationAuditReqDTO);
    }
}

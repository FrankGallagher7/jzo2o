package com.jzo2o.customer.controller.operation;

import cn.hutool.core.bean.BeanUtil;
import com.jzo2o.common.model.PageResult;
import com.jzo2o.customer.mapper.WorkerCertificationAuditMapper;
import com.jzo2o.customer.model.domain.WorkerCertification;
import com.jzo2o.customer.model.domain.WorkerCertificationAudit;
import com.jzo2o.customer.model.dto.request.CertificationAuditReqDTO;
import com.jzo2o.customer.model.dto.request.WorkerCertificationAuditPageQueryReqDTO;
import com.jzo2o.customer.model.dto.response.WorkerCertificationResDTO;
import com.jzo2o.customer.service.IWorkerCertificationAuditService;
import com.jzo2o.customer.service.IWorkerCertificationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @author itcast
 */
@RestController("workerCertificationAuditController")
@RequestMapping("/operation/worker-certification-audit")
@Api(tags = "运营端 - 服务人员审核相关接口")
public class WorkerCertificationAuditController {
    @Autowired
    private IWorkerCertificationAuditService workerCertificationAuditService;

    @GetMapping("/page")
    @ApiOperation("审核服务人员认证分页查询")
    public PageResult<WorkerCertificationAudit> pageQuery(WorkerCertificationAuditPageQueryReqDTO workerCertificationAuditPageQueryReqDTO) {
        return workerCertificationAuditService.pageQuery(workerCertificationAuditPageQueryReqDTO);
    }

    @PutMapping("/audit/{id}")
    @ApiOperation("审核服务人员认证")
    public void audit(@PathVariable Long id, @RequestBody CertificationAuditReqDTO certificationAuditReqDTO) {
        workerCertificationAuditService.audit(id, certificationAuditReqDTO);
    }
}

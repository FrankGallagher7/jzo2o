package com.jzo2o.customer.controller.agency;


import com.jzo2o.customer.model.dto.request.AgencyCertificationAuditAddReqDTO;
import com.jzo2o.customer.model.dto.request.CertificationAuditReqDTO;
import com.jzo2o.customer.model.dto.request.WorkerCertificationAuditAddReqDTO;
import com.jzo2o.customer.service.IAgencyCertificationService;
import com.jzo2o.customer.service.IWorkerCertificationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 银行账户表 前端控制器
 * </p>
 *
 * @author itcast
 * @since 2023-07-18
 */
@RestController("agencyCertificationController")
@RequestMapping("/agency/agency-certification-audit")
@Api(tags = "pc服务端 - 认证相关接口")
public class AgencyCertificationController {
    @Autowired
    private IAgencyCertificationService agencyCertificationService;

    @PostMapping
    @ApiOperation("pc新增认证申请")
    public void postCertification(@RequestBody AgencyCertificationAuditAddReqDTO agencyCertificationAuditAddReqDTO) {
        agencyCertificationService.postCertification(agencyCertificationAuditAddReqDTO);
    }

    @ApiOperation("pc获取拒绝原因")
    @GetMapping("/rejectReason")
    public CertificationAuditReqDTO getRejectReason() {
        return agencyCertificationService.getRejectReason();
    }
}

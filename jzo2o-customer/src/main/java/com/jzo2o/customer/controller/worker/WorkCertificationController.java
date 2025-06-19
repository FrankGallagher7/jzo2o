package com.jzo2o.customer.controller.worker;


import com.jzo2o.customer.model.dto.request.BankAccountUpsertReqDTO;
import com.jzo2o.customer.model.dto.request.WorkerCertificationAuditAddReqDTO;
import com.jzo2o.customer.model.dto.response.BankAccountResDTO;
import com.jzo2o.customer.service.BankAccountService;
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
@RestController("workCertificationController")
@RequestMapping("/worker/worker-certification-audit")
@Api(tags = "app服务端 - 认证相关接口")
public class WorkCertificationController {
    @Autowired
    private IWorkerCertificationService workerCertificationService;

    @PostMapping
    @ApiOperation("app新增认证申请")
    public void postCertification(@RequestBody WorkerCertificationAuditAddReqDTO workerCertificationAuditAddReqDTO) {
        workerCertificationService.postCertification(workerCertificationAuditAddReqDTO);
    }
}

package com.jzo2o.customer.controller.worker;


import com.jzo2o.customer.model.dto.request.BankAccountUpsertReqDTO;
import com.jzo2o.customer.service.BankAccountService;
import com.jzo2o.mvc.utils.UserContext;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 银行账户表 前端控制器
 * </p>
 *
 * @author itcast
 * @since 2023-07-18
 */
@RestController("workerServeBankController")
@RequestMapping("/worker/bank-account")
@Api(tags = "服务端 - 服务银行相关接口")
public class BankAccountController {

    @Autowired
    private BankAccountService bankAccountService;

    @PostMapping
    @ApiOperation("app新增或修改银行信息")
    public void putBankAccount(@RequestBody BankAccountUpsertReqDTO bankAccountUpsertReqDTO) {
        bankAccountService.putBankAccount(bankAccountUpsertReqDTO);
    }
}

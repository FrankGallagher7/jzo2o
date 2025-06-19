package com.jzo2o.customer.controller.worker;


import com.jzo2o.customer.model.dto.request.BankAccountUpsertReqDTO;
import com.jzo2o.customer.model.dto.response.BankAccountResDTO;
import com.jzo2o.customer.service.BankAccountService;
import com.jzo2o.mvc.utils.UserContext;
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

    @ApiOperation("查询当前登录用户的银行信息")
    @GetMapping("/currentUserBankAccount")
    public BankAccountResDTO currentUserBankAccount() {
        return bankAccountService.currentUserBankAccount();
    }
}

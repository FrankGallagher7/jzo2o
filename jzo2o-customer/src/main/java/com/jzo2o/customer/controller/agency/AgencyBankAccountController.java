package com.jzo2o.customer.controller.agency;


import com.jzo2o.customer.model.dto.request.BankAccountUpsertReqDTO;
import com.jzo2o.customer.model.dto.response.BankAccountResDTO;
import com.jzo2o.customer.service.BankAccountService;
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
@RestController("workerAgencyServeBankController")
@RequestMapping("/agency/bank-account")
@Api(tags = "pc服务端 - pc服务银行相关接口")
public class AgencyBankAccountController {

    @Autowired
    private BankAccountService bankAccountService;

    @PostMapping
    @ApiOperation("pc新增或修改银行信息")
    public void putBankAccount(@RequestBody BankAccountUpsertReqDTO bankAccountUpsertReqDTO) {
        bankAccountService.putBankAccount(bankAccountUpsertReqDTO);
    }

    @ApiOperation("pc查询当前登录用户的银行信息")
    @GetMapping("/currentUserBankAccount")
    public BankAccountResDTO currentUserBankAccount() {
        return bankAccountService.currentUserBankAccount();
    }
}

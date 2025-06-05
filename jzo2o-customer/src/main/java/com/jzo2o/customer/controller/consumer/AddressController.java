package com.jzo2o.customer.controller.consumer;

import com.jzo2o.api.customer.dto.response.AddressBookResDTO;
import com.jzo2o.customer.model.domain.AddressBook;
import com.jzo2o.customer.service.IAddressBookService;
import com.jzo2o.customer.service.ICommonUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 普通用户相关接口
 *
 * @author itcast
 * @create 2023/7/7 19:34
 **/
@RestController("consumerAddressController")
@RequestMapping("/consumer/address-book")
@Api(tags = "用户端 - 地址相关接口")
public class AddressController {


    @Autowired
    private IAddressBookService addressBookService;

    /**
     * 查询用户默认地址值
     * @return
     */
    @GetMapping("/defaultAddress")
    @ApiOperation("查询用户默认地址值")
    public AddressBookResDTO findDefaultAddress(){
        return addressBookService.findDefaultAddress();
    }

    /**
     * 新增地址
     * @param addressBookResDTO
     */
    @PostMapping
    @ApiOperation("新增地址")
    public void saveAddress(@RequestBody AddressBookResDTO addressBookResDTO){
        addressBookService.saveAddress(addressBookResDTO);
    }

}

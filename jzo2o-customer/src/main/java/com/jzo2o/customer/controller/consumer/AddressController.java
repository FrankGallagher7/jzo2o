package com.jzo2o.customer.controller.consumer;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.jzo2o.api.customer.dto.response.AddressBookResDTO;
import com.jzo2o.common.model.PageResult;
import com.jzo2o.customer.model.domain.AddressBook;
import com.jzo2o.customer.model.dto.request.AddressBookPageQueryReqDTO;
import com.jzo2o.customer.service.IAddressBookService;
import com.jzo2o.customer.service.ICommonUserService;
import com.jzo2o.mvc.utils.UserContext;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

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

    /**
     * 分页查询地址
     * @param pageQueryReqDTO
     * @return
     */
    @GetMapping("/page")
    @ApiOperation("分页查询地址")
    public PageResult<AddressBookResDTO> page(AddressBookPageQueryReqDTO pageQueryReqDTO) {
        return addressBookService.pageQuery(pageQueryReqDTO);
    }

    /**
     * 根据地址id查询地址详情
     */
    @GetMapping("/{id}")
    @ApiOperation("根据地址id查询地址详情")
    public AddressBookResDTO getById(@PathVariable Long id) {
        AddressBook addressBook = addressBookService.getById(id);
        return BeanUtil.copyProperties(addressBook, AddressBookResDTO.class);
    }

    /**
     * 根据地址id对地址进行编辑
     * @param id
     * @param addressBookResDTO
     */
    @PutMapping("/{id}")
    @ApiOperation("根据地址id对地址进行编辑")
    public void update(@PathVariable Long id, @RequestBody AddressBookResDTO addressBookResDTO) {
        addressBookService.updateAddress(id, addressBookResDTO);
    }

    /**
     * 批量删除地址
     * @param ids
     */
    @DeleteMapping("/batch")
    @ApiOperation("批量删除地址")
    public void deleteBatch(@RequestBody List<Long> ids) {
        addressBookService.removeUpdateByIds(ids);
    }

}

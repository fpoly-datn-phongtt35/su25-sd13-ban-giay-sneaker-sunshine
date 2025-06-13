package com.example.duantotnghiep.service.impl;

import com.example.duantotnghiep.dto.request.AddressCustomerRequest;
import com.example.duantotnghiep.dto.request.CustomerRequest;
import com.example.duantotnghiep.dto.response.AddressCustomerResponse;
import com.example.duantotnghiep.dto.response.CustomerResponse;
import com.example.duantotnghiep.mapper.AddressMapper;
import com.example.duantotnghiep.mapper.CustomerMapper;
import com.example.duantotnghiep.mapper.InvoiceMapper;
import com.example.duantotnghiep.mapper.UserMapper;
import com.example.duantotnghiep.model.AddressCustomer;
import com.example.duantotnghiep.model.Customer;
import com.example.duantotnghiep.model.User;
import com.example.duantotnghiep.repository.AddressRepository;
import com.example.duantotnghiep.repository.CustomerRepository;
import com.example.duantotnghiep.repository.UserRepository;
import com.example.duantotnghiep.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {
    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final CustomerMapper customerMapper;
    private final UserMapper userMapper;
    private final AddressMapper addressMapper;

    @Transactional(rollbackFor = Throwable.class)
    @Override
    public CustomerResponse createCustomer(CustomerRequest request) {
        // 1. Chuyển CustomerRequest thành Customer entity
        Customer customer = userMapper.toCustomerEntity(request);
        customer.setCustomerCode(generateCustomerCode());
        customer.setCreatedDate(LocalDateTime.now());
        customer.setCreatedBy("admin");
        customer.setStatus(1);

        // 2. Lưu customer trước để có id cho các quan hệ liên quan
        customerRepository.save(customer);

        // 3. Tạo AddressCustomer
        AddressCustomer address = addressMapper.toAddressCustomerEntitty(request);
        address.setCreatedDate(new Date());
        address.setCreatedBy("admin");
        address.setStatus(1);
        address.setDefaultAddress(true);
        address.setCustomer(customer); // gán quan hệ
        addressRepository.save(address); // lưu address sau khi có customer

        // 4. Set địa chỉ mặc định

        customerRepository.save(customer); // cập nhật lại customer để set default address

        // 5. Tạo user login
        User user = userMapper.toCustomerUserEntity(request);
        user.setCreatedAt(new Date());
        user.setCreatedBy("admin");
        user.setRole(3);
        user.setCustomer(customer);
        userRepository.save(user);

        // 6. Trả về response
        return customerMapper.toDto(customer);
    }

    @Transactional
    @Override
    public CustomerResponse updateCustomer(Long id, CustomerRequest request) {
        // Tìm user theo customerId
        User user = userRepository.findByCustomerId(id)
                .orElseThrow(() -> new RuntimeException("Customer not found with id: " + id));

        // Lấy ra customer từ user
        Customer customer = user.getCustomer();
        if (customer == null) {
            throw new RuntimeException("User không chứa thông tin customer");
        }

        // Cập nhật thông tin customer từ request
        userMapper.updateCustomerFromRequest(request, customer);
        customer.setUpdatedBy("admin"); // hoặc người dùng đang đăng nhập
        customer.setUpdatedDate(LocalDateTime.now());

        // Cập nhật thông tin user
        user.setUsername(request.getUsername());
        user.setPassword(request.getPassword());
        user.setUpdatedAt(new Date());
        user.setUpdatedBy("admin");

        // Gán lại customer vào user nếu cần
        user.setCustomer(customer);

        // Lưu lại cả hai
        customerRepository.save(customer);
        userRepository.save(user);

        // Trả về DTO phản hồi
        return userMapper.toCustomerResponse(user);
    }


    @Transactional
    @Override
    public void deleteCustomer(Long id) {
        Customer customer = customerRepository.findCustomerByIdAndStatus(id,1)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng với ID: " + id));

        customer.setStatus(0);
        customer.setUpdatedDate(LocalDateTime.now());
        customer.setUpdatedBy("admin"); // hoặc username hiện tại nếu có
        customerRepository.save(customer);

        List<AddressCustomer> addressList = addressRepository.findByCustomerIdAndStatusOrderByDefaultAddressDesc(id,1);
        for (AddressCustomer address : addressList) {
            address.setStatus(0);
            address.setUpdatedDate(new Date());
            address.setUpdatedBy("admin");
        }
        addressRepository.saveAll(addressList);
    }

    @Override
    public CustomerResponse getCustomerById(Long id) {
        User user = userRepository.findByCustomerId(id)
                .orElseThrow(null);
        return userMapper.toCustomerResponse(user);
    }

    @Override
    public List<CustomerResponse> getAllCustomers() {
        return customerRepository.findAll().stream()
                .map(customerMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public Page<CustomerResponse> findCustomerByStatus(Pageable pageable) {
        Page<Customer> customers = customerRepository.findCustomerByStatus1(1, pageable);
        return customers.map(customerMapper::toDto);
    }

    @Override
    public List<AddressCustomerResponse> getByCustomerId(Long customerId) {
        return addressRepository.findByCustomerIdAndStatusOrderByDefaultAddressDesc(customerId,1)
                .stream()
                .map(addressMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public AddressCustomerResponse create(AddressCustomerRequest request) {
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        AddressCustomer address = addressMapper.toEntity(request);
        address.setCustomer(customer);
        address.setDefaultAddress(false);
        address.setCreatedDate(new Date());
        address.setCreatedBy("admin"); // có thể là user đang đăng nhập
        address.setStatus(1);

        return addressMapper.toResponse(addressRepository.save(address));
    }

    @Override
    public AddressCustomerResponse getAddressById(Long id) {
        AddressCustomer address = addressRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Address not found"));

        return addressMapper.toResponse(address);
    }

    @Override
    public AddressCustomerResponse update(Long id, AddressCustomerRequest request) {
        AddressCustomer address = addressRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Address not found"));

        addressMapper.updateEntityFromRequest(request, address);
        address.setUpdatedDate(new Date());
        address.setUpdatedBy("admin");

        return addressMapper.toResponse(addressRepository.save(address));
    }

    @Override
    public void deleteAddressCustomer(Long id) {
        AddressCustomer address = addressRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Address not found"));

        address.setStatus(0);
        address.setUpdatedDate(new Date());
        address.setUpdatedBy("admin");
        addressRepository.save(address);

    }

    @Transactional
    @Override
    public void setDefaultAddress(Long customerId, Long addressId) {
        // Bước 1: Set tất cả địa chỉ của customerId về false
        addressRepository.clearDefaultAddress(customerId);

        // Bước 2: Set địa chỉ được chọn thành true
        AddressCustomer address = addressRepository.findById(addressId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy địa chỉ"));

        if (!address.getCustomer().getId().equals(customerId)) {
            throw new RuntimeException("Địa chỉ không thuộc khách hàng này");
        }

        address.setDefaultAddress(true);
        addressRepository.save(address); // Chỉ cần save địa chỉ được chọn
    }

    private String generateCustomerCode() {
        String prefix = "CUSTOMER-";
        String datePart = new SimpleDateFormat("yyyyMMdd").format(new Date());
        String randomPart = String.format("%04d", (int) (Math.random() * 10000));
        return prefix + datePart + "-" + randomPart;
    }

}

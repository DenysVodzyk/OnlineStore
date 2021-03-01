package service;

import entity.Customer;
import entity.Gender;
import entity.Item;
import entity.Order;
import repository.CustomerRepository;
import utils.FileReader;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class CustomerService {
    private final DateTimeFormatter DOB = DateTimeFormatter.ofPattern("d MMMM yyyy");
    private final DateTimeFormatter LAST_PURCHASE_DATE = DateTimeFormatter.ofPattern("M/d/yyyy");
    private ItemService itemService;
    private OrderService orderService;
    private CustomerRepository customerRepository;

    public CustomerService() {
        init();
        this.customerRepository = new CustomerRepository();
        this.orderService = new OrderService();
    }

    private void init() {
        this.itemService = new ItemService();
    }

    public List<Customer> parseAll(String customersFilePath) {
        List<String> fileData = FileReader.readFromFile(customersFilePath);
        List<Customer> customers = new ArrayList<>();

        for (String customer : fileData) {
            String[] customerData = customer.split("[;:]");
            String name = customerData[0];
            LocalDate dOB = LocalDate.parse(customerData[1], DOB);
            String address = trimFirstLastCharacter(customerData[2]);
            Gender gender = GenderService.getGender(customerData[3]);
            String phoneNumber = customerData[4];
            List<Item> lastPurchases = getLastPurchases(customerData[5]);
            LocalDate dateOfLastPurchase = LocalDate.parse(customerData[6], LAST_PURCHASE_DATE);

            if ((phoneNumber.isEmpty())) {
                customers.add(new Customer(name, dOB, address, gender, "", lastPurchases, dateOfLastPurchase));
            } else {
                customers.add(new Customer(name, dOB, address, gender, phoneNumber, lastPurchases, dateOfLastPurchase));
            }
        }
        return customers;
    }

    public String trimFirstLastCharacter(String input) {
        return input.substring(1, input.length() - 1);
    }

    //fetch last purchases done by customer. Based on the id of the item, find item from existing list of items and add it to item collection.
    public List<Item> getLastPurchases(String purchases) {
        List<Item> lastPurchases = new ArrayList<>();

        String purchase = (purchases.length() <= 2) ? purchases : trimFirstLastCharacter(purchases);
        for (String item : purchase.split(",")) {
            int itemId = Integer.parseInt(item);
            lastPurchases.add(itemService.getById(itemId));
        }
        return lastPurchases;
    }

    public void addAllToDB(List<Customer> customers) {
        for (Customer customer : customers) {
            customerRepository.add(customer);
        }
    }

    public List<Customer> getAll() {
        List<Customer> customers = new ArrayList<>();
        for (Customer customer : customerRepository.getAll()) {
            setOrderToCustomer(customer);
            customers.add(customer);
        }
        return customers;
    }

    public Customer getById(int id) {
        Customer customer = customerRepository.getById(id);
        setOrderToCustomer(customer);
        return customer;
    }

    public Customer getByName(String name) {
        Customer customer = customerRepository.getByName(name);
        setOrderToCustomer(customer);
        return customer;
    }

    public void setOrderToCustomer(Customer customer) {
        Order order = orderService.getByCustomerId(getId(customer));
        customer.setOrder(order);
    }

    public int getId(Customer customer) {
        return customerRepository.getId(customer);
    }

    public List<Customer> getCustomersByGender(List<Customer> customers, Gender gender) {
        List<Customer> sortedCustomers = new ArrayList<>();
        for (Customer customer : customers) {
            if (customer.getGender() == gender)
                sortedCustomers.add(customer);
        }
        return sortedCustomers;
    }
}

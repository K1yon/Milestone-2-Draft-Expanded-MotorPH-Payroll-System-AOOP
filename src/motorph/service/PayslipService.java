package motorph.service;

import java.time.LocalDate;
import motorph.model.Payslip;
import motorph.model.PayslipEntity;
import motorph.repository.PayslipRepository;

public class PayslipService {

    private final PayslipRepository payslipRepository = new PayslipRepository();

    public void saveGeneratedPayslip(Payslip payslip,
                                     LocalDate payPeriodStart,
                                     LocalDate payPeriodEnd) {

        PayslipEntity entity = new PayslipEntity(
                payslip.getEmployeeNumber(),
                payPeriodStart,
                payPeriodEnd,
                payslip.getGrossPay(),
                payslip.getTotalDeductions(),
                payslip.getNetPay(),
                payslip.getSssDeduction(),
                payslip.getPhilhealthDeduction(),
                payslip.getPagibigDeduction(),
                payslip.getWithholdingTax()
        );

        payslipRepository.save(entity);
    }
}
package com.astrotech.transport.validators.seats;




import com.astrotech.transport.repositories.SeatRepository;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;

public class SeatIdsValidator implements ConstraintValidator<ValidSeatIds, List<Long>> {

    @Autowired
    private SeatRepository seatRepository;

    @Override
    public boolean isValid(List<Long> seatIds, ConstraintValidatorContext context) {

        if (seatIds == null || seatIds.isEmpty()) {
            return true;
        }


        var existingSeatsCount = seatRepository.countByIdIn(seatIds);


        return existingSeatsCount == seatIds.size();
    }
}


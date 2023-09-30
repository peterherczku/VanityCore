package dev.blockeed.vanitycore.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class Int {

    private Integer amount;

    public void increase(Integer amount) {
        this.amount+=amount;
    }

    public void decrease(Integer amount) {
        this.amount-=amount;
    }

}

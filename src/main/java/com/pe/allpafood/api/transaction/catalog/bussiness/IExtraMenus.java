package com.pe.allpafood.api.transaction.catalog.bussiness;

import java.time.LocalDate;
import java.util.List;

public interface IExtraMenus {
    List<Integer> getMenuTypeIds(LocalDate scheduledDate, List<String> extraBenefits);
}

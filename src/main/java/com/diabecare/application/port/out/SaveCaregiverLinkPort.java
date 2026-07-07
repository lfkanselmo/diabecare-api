package com.diabecare.application.port.out;

import com.diabecare.domain.model.CaregiverLink;

public interface SaveCaregiverLinkPort {
    CaregiverLink save(CaregiverLink link);
}

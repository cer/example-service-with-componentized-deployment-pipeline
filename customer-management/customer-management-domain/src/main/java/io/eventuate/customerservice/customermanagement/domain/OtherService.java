package io.eventuate.customerservice.customermanagement.domain;

import java.util.Optional;

public interface OtherService {

  Optional<OtherDetails> findOther(String otherId);
}

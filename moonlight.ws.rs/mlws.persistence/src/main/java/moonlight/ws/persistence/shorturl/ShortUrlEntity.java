package moonlight.ws.persistence.shorturl;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import lombok.Getter;
import lombok.Setter;
import moonlight.ws.persistence.AbstractEntity;

@Getter
@Setter
@Entity(name = "ShortUrl")
public class ShortUrlEntity extends AbstractEntity {

	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ShortUrlIdSequence")
	@SequenceGenerator(name = "ShortUrlIdSequence", sequenceName = "ShortUrlIdSequence", allocationSize = 1)
	private Long id;

	@Override
	public Long getId() {
		return id;
	}

	private String code;

	private String longUrl;

	private String shortUrl;
}

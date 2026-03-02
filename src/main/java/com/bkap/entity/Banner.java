package com.bkap.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "banners")
public class Banner {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "title" ,length = 200)
    private String title;

    @Column(name = "image")
    private String image;

    @Column(name = "prioty")
    private Integer prioty;

    @Column(name = "status")
    private Boolean status;

    @Column(name = "link" ,length = 500)
    private String link;

    @Column(name = "position" ,length = 100)
    private String position;

    public Banner() {
    	//
    }

	public Banner(Integer id, String title, String image, Integer prioty, Boolean status, String link,
			String position) {
		super();
		this.id = id;
		this.title = title;
		this.image = image;
		this.prioty = prioty;
		this.status = status;
		this.link = link;
		this.position = position;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public Integer getPrioty() {
		return prioty;
	}

	public void setPrioty(Integer prioty) {
		this.prioty = prioty;
	}

	public Boolean getStatus() {
		return status;
	}

	public void setStatus(Boolean status) {
		this.status = status;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public String getPosition() {
		return position;
	}

	public void setPosition(String position) {
		this.position = position;
	}
    
    
}

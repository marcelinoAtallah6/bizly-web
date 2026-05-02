import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ApibuilderComponent } from './apibuilder.component';

describe('ApibuilderComponent', () => {
  let component: ApibuilderComponent;
  let fixture: ComponentFixture<ApibuilderComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ApibuilderComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(ApibuilderComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

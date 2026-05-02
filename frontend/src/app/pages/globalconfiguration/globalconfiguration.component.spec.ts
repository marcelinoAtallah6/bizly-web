import { ComponentFixture, TestBed } from '@angular/core/testing';

import { GlobalconfigurationComponent } from './globalconfiguration.component';

describe('GlobalconfigurationComponent', () => {
  let component: GlobalconfigurationComponent;
  let fixture: ComponentFixture<GlobalconfigurationComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [GlobalconfigurationComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(GlobalconfigurationComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

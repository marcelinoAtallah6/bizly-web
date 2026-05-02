import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DashboardbuilderComponent } from './dashboardbuilder.component';

describe('DashboardbuilderComponent', () => {
  let component: DashboardbuilderComponent;
  let fixture: ComponentFixture<DashboardbuilderComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DashboardbuilderComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(DashboardbuilderComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

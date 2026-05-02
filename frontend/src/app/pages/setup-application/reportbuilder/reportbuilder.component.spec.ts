import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ReportbuilderComponent } from './reportbuilder.component';

describe('ReportbuilderComponent', () => {
  let component: ReportbuilderComponent;
  let fixture: ComponentFixture<ReportbuilderComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ReportbuilderComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(ReportbuilderComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

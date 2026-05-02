import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DropdownRendererComponent } from './dropdown-renderer.component';

describe('DropdownRendererComponent', () => {
  let component: DropdownRendererComponent;
  let fixture: ComponentFixture<DropdownRendererComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DropdownRendererComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(DropdownRendererComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

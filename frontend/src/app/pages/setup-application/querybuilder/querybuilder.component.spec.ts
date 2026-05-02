import { ComponentFixture, TestBed } from '@angular/core/testing';

import { QuerybuilderComponent } from './querybuilder.component';

describe('QuerybuilderComponent', () => {
  let component: QuerybuilderComponent;
  let fixture: ComponentFixture<QuerybuilderComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [QuerybuilderComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(QuerybuilderComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

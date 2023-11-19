import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ReservationCalendarComponent } from './reservation-list.component';

describe('ReservationListComponent', () => {
  let component: ReservationCalendarComponent;
  let fixture: ComponentFixture<ReservationCalendarComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [ReservationCalendarComponent]
    });
    fixture = TestBed.createComponent(ReservationCalendarComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

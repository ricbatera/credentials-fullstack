import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Configs } from './configs';

describe('Configs', () => {
  let component: Configs;
  let fixture: ComponentFixture<Configs>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Configs]
    })
    .compileComponents();

    fixture = TestBed.createComponent(Configs);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
